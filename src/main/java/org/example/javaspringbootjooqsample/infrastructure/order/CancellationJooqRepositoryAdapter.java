package org.example.javaspringbootjooqsample.infrastructure.order;

import lombok.RequiredArgsConstructor;
import org.example.javaspringbootjooqsample.domain.order.Cancellation;
import org.example.javaspringbootjooqsample.domain.order.repository.CancellationRepository;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

import static org.example.javaspringbootjooqsample.generated.jooq_codegen.tables.Cancellations.CANCELLATIONS;

@Repository
@RequiredArgsConstructor
public class CancellationJooqRepositoryAdapter implements CancellationRepository {

    private final DSLContext dsl;

    @Override
    public Cancellation save(Cancellation cancellation) {
        if (cancellation.getId() == null) {
            Long insertedId = dsl.insertInto(CANCELLATIONS)
                    .set(CANCELLATIONS.ORDER_ID, cancellation.getOrderId())
                    .set(CANCELLATIONS.IDEMPOTENCY_KEY, cancellation.getIdempotencyKey())
                    .set(CANCELLATIONS.REASON, cancellation.getReason())
                    .set(CANCELLATIONS.STATUS, cancellation.getStatus())
                    .set(CANCELLATIONS.REFUNDED_AT, cancellation.getRefundedAt())
                    .set(CANCELLATIONS.VERSION, cancellation.getVersion())
                    .set(CANCELLATIONS.CREATED_AT, DSL.currentLocalDateTime())
                    .set(CANCELLATIONS.UPDATED_AT, DSL.currentLocalDateTime())
                    .returning(CANCELLATIONS.ID)
                    .fetchOne(CANCELLATIONS.ID);
            cancellation.setId(insertedId);
            return cancellation;
        }

        Long currentVersion = cancellation.getVersion() == null ? 0L : cancellation.getVersion();
        LocalDateTime now = LocalDateTime.now();
        int updated = dsl.update(CANCELLATIONS)
                .set(CANCELLATIONS.STATUS, cancellation.getStatus())
                .set(CANCELLATIONS.REFUNDED_AT, cancellation.getRefundedAt())
                .set(CANCELLATIONS.VERSION, CANCELLATIONS.VERSION.plus(1L))
                .set(CANCELLATIONS.UPDATED_AT, now)
                .where(CANCELLATIONS.ID.eq(cancellation.getId()))
                .and(CANCELLATIONS.VERSION.eq(currentVersion))
                .execute();

        if (updated == 1) {
            cancellation.setVersion(currentVersion + 1);
            cancellation.setUpdatedAt(now);
        }

        return cancellation;
    }

    @Override
    public Cancellation findById(Long id) {
        if (id == null) {
            return null;
        }

        return dsl.selectFrom(CANCELLATIONS)
                .where(CANCELLATIONS.ID.eq(id))
                .fetchOne(this::toCancellation);
    }

    @Override
    public Cancellation findByIdempotencyKey(String idempotencyKey) {
        if (idempotencyKey == null) {
            return null;
        }

        return dsl.selectFrom(CANCELLATIONS)
                .where(CANCELLATIONS.IDEMPOTENCY_KEY.eq(idempotencyKey))
                .fetchOne(this::toCancellation);
    }

    @Override
    public List<Cancellation> findByOrderId(Long orderId) {
        if (orderId == null) {
            return List.of();
        }

        return dsl.selectFrom(CANCELLATIONS)
                .where(CANCELLATIONS.ORDER_ID.eq(orderId))
                .orderBy(CANCELLATIONS.ID.asc())
                .fetch(this::toCancellation);
    }

    private Cancellation toCancellation(Record record) {
        return Cancellation.builder()
                .id(record.get(CANCELLATIONS.ID))
                .orderId(record.get(CANCELLATIONS.ORDER_ID))
                .idempotencyKey(record.get(CANCELLATIONS.IDEMPOTENCY_KEY))
                .reason(record.get(CANCELLATIONS.REASON))
                .status(record.get(CANCELLATIONS.STATUS))
                .refundedAt(record.get(CANCELLATIONS.REFUNDED_AT))
                .version(record.get(CANCELLATIONS.VERSION))
                .createdAt(record.get(CANCELLATIONS.CREATED_AT))
                .updatedAt(record.get(CANCELLATIONS.UPDATED_AT))
                .build();
    }
}
