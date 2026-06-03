package org.example.javaspringbootjooqsample.infrastructure.outbox;

import lombok.RequiredArgsConstructor;
import org.example.javaspringbootjooqsample.domain.outbox.OutboxEvent;
import org.example.javaspringbootjooqsample.domain.outbox.repository.OutboxEventRepository;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

import static org.example.javaspringbootjooqsample.generated.jooq_codegen.tables.OutboxEvents.OUTBOX_EVENTS;

@Repository
@RequiredArgsConstructor
public class OutboxEventJooqRepositoryAdapter implements OutboxEventRepository {

    private final DSLContext dsl;

    @Override
    public OutboxEvent save(OutboxEvent event) {
        if (event.getId() == null) {
            Long insertedId = dsl.insertInto(OUTBOX_EVENTS)
                    .set(OUTBOX_EVENTS.EVENT_ID, event.getEventId())
                    .set(OUTBOX_EVENTS.AGGREGATE_TYPE, event.getAggregateType())
                    .set(OUTBOX_EVENTS.AGGREGATE_ID, event.getAggregateId())
                    .set(OUTBOX_EVENTS.EVENT_TYPE, event.getEventType())
                    .set(OUTBOX_EVENTS.PAYLOAD, event.getPayload())
                    .set(OUTBOX_EVENTS.STATUS, event.getStatus())
                    .set(OUTBOX_EVENTS.RETRY_COUNT, event.getRetryCount())
                    .set(OUTBOX_EVENTS.NEXT_ATTEMPT_AT, event.getNextAttemptAt())
                    .set(OUTBOX_EVENTS.PUBLISHED_AT, event.getPublishedAt())
                    .set(OUTBOX_EVENTS.LAST_ERROR, event.getLastError())
                    .set(OUTBOX_EVENTS.CREATED_AT, event.getCreatedAt())
                    .returning(OUTBOX_EVENTS.ID)
                    .fetchOne(OUTBOX_EVENTS.ID);
            event.setId(insertedId);
            return event;
        }

        dsl.update(OUTBOX_EVENTS)
                .set(OUTBOX_EVENTS.STATUS, event.getStatus())
                .set(OUTBOX_EVENTS.RETRY_COUNT, event.getRetryCount())
                .set(OUTBOX_EVENTS.NEXT_ATTEMPT_AT, event.getNextAttemptAt())
                .set(OUTBOX_EVENTS.PUBLISHED_AT, event.getPublishedAt())
                .set(OUTBOX_EVENTS.LAST_ERROR, event.getLastError())
                .where(OUTBOX_EVENTS.ID.eq(event.getId()))
                .execute();

        return event;
    }

    @Override
    public OutboxEvent findById(Long id) {
        if (id == null) {
            return null;
        }

        return dsl.selectFrom(OUTBOX_EVENTS)
                .where(OUTBOX_EVENTS.ID.eq(id))
                .fetchOne(this::toOutboxEvent);
    }

    @Override
    public OutboxEvent findByEventId(String eventId) {
        if (eventId == null) {
            return null;
        }

        return dsl.selectFrom(OUTBOX_EVENTS)
                .where(OUTBOX_EVENTS.EVENT_ID.eq(eventId))
                .fetchOne(this::toOutboxEvent);
    }

    @Override
    public List<OutboxEvent> findPendingForUpdate(LocalDateTime now, int limit) {
        return dsl.selectFrom(OUTBOX_EVENTS)
                .where(OUTBOX_EVENTS.STATUS.eq(org.example.javaspringbootjooqsample.domain.outbox.OutboxStatus.PENDING))
                .and(OUTBOX_EVENTS.NEXT_ATTEMPT_AT.le(now))
                .orderBy(OUTBOX_EVENTS.ID.asc())
                .limit(limit)
                .forUpdate()
                .skipLocked()
                .fetch(this::toOutboxEvent);
    }

    private OutboxEvent toOutboxEvent(Record record) {
        Integer retryCount = record.get(OUTBOX_EVENTS.RETRY_COUNT);
        return OutboxEvent.builder()
                .id(record.get(OUTBOX_EVENTS.ID))
                .eventId(record.get(OUTBOX_EVENTS.EVENT_ID))
                .aggregateType(record.get(OUTBOX_EVENTS.AGGREGATE_TYPE))
                .aggregateId(record.get(OUTBOX_EVENTS.AGGREGATE_ID))
                .eventType(record.get(OUTBOX_EVENTS.EVENT_TYPE))
                .payload(record.get(OUTBOX_EVENTS.PAYLOAD))
                .status(record.get(OUTBOX_EVENTS.STATUS))
                .retryCount(retryCount == null ? 0 : retryCount)
                .nextAttemptAt(record.get(OUTBOX_EVENTS.NEXT_ATTEMPT_AT))
                .publishedAt(record.get(OUTBOX_EVENTS.PUBLISHED_AT))
                .lastError(record.get(OUTBOX_EVENTS.LAST_ERROR))
                .createdAt(record.get(OUTBOX_EVENTS.CREATED_AT))
                .build();
    }
}
