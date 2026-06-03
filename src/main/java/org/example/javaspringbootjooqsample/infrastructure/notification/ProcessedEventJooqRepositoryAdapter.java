package org.example.javaspringbootjooqsample.infrastructure.notification;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Component;

import static org.example.javaspringbootjooqsample.generated.jooq_codegen.tables.ProcessedEvents.PROCESSED_EVENTS;

@Component
@RequiredArgsConstructor
public class ProcessedEventJooqRepositoryAdapter {

    private final DSLContext dsl;

    public int insertEvent(ProcessedEvent event) {
        return dsl.insertInto(PROCESSED_EVENTS)
                .set(PROCESSED_EVENTS.EVENT_ID, event.getEventId())
                .set(PROCESSED_EVENTS.CONSUMER_NAME, event.getConsumerName())
                .set(PROCESSED_EVENTS.PROCESSED_AT, event.getProcessedAt())
                .execute();
    }

    public boolean existsByEventIdAndConsumerName(String eventId, String consumerName) {
        return dsl.fetchExists(
                dsl.selectOne()
                        .from(PROCESSED_EVENTS)
                        .where(PROCESSED_EVENTS.EVENT_ID.eq(eventId))
                        .and(PROCESSED_EVENTS.CONSUMER_NAME.eq(consumerName))
        );
    }

    public int deleteByEventIdAndConsumerName(String eventId, String consumerName) {
        return dsl.deleteFrom(PROCESSED_EVENTS)
                .where(PROCESSED_EVENTS.EVENT_ID.eq(eventId))
                .and(PROCESSED_EVENTS.CONSUMER_NAME.eq(consumerName))
                .execute();
    }
}
