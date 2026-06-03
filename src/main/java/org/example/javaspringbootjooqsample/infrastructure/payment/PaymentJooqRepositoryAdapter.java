package org.example.javaspringbootjooqsample.infrastructure.payment;

import lombok.RequiredArgsConstructor;
import org.example.javaspringbootjooqsample.domain.payment.Payment;
import org.example.javaspringbootjooqsample.domain.payment.PaymentHistory;
import org.example.javaspringbootjooqsample.domain.payment.repository.PaymentRepository;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.example.javaspringbootjooqsample.generated.jooq_codegen.tables.PaymentHistories.PAYMENT_HISTORIES;
import static org.example.javaspringbootjooqsample.generated.jooq_codegen.tables.Payments.PAYMENTS;

@Repository
@RequiredArgsConstructor
public class PaymentJooqRepositoryAdapter implements PaymentRepository {

    private final DSLContext dsl;

    @Override
    public Payment save(Payment payment) {
        if (payment.getId() == null) {
            Long insertedId = dsl.insertInto(PAYMENTS)
                    .set(PAYMENTS.ORDER_ID, payment.getOrderId())
                    .set(PAYMENTS.IDEMPOTENCY_KEY, payment.getIdempotencyKey())
                    .set(PAYMENTS.AMOUNT, payment.getAmount())
                    .set(PAYMENTS.STATUS, payment.getStatus())
                    .set(PAYMENTS.PAYMENT_KEY, payment.getPaymentKey())
                    .set(PAYMENTS.APPROVED_AT, payment.getApprovedAt())
                    .set(PAYMENTS.REFUNDED_AT, payment.getRefundedAt())
                    .set(PAYMENTS.VERSION, payment.getVersion())
                    .returning(PAYMENTS.ID)
                    .fetchOne(PAYMENTS.ID);

            payment.setId(insertedId);
        } else {
            Long currentVersion = payment.getVersion() == null ? 0L : payment.getVersion();
            LocalDateTime now = LocalDateTime.now();
            int updated = dsl.update(PAYMENTS)
                    .set(PAYMENTS.STATUS, payment.getStatus())
                    .set(PAYMENTS.PAYMENT_KEY, payment.getPaymentKey())
                    .set(PAYMENTS.APPROVED_AT, payment.getApprovedAt())
                    .set(PAYMENTS.REFUNDED_AT, payment.getRefundedAt())
                    .set(PAYMENTS.VERSION, PAYMENTS.VERSION.plus(1L))
                    .set(PAYMENTS.UPDATED_AT, now)
                    .where(PAYMENTS.ID.eq(payment.getId()))
                    .and(PAYMENTS.VERSION.eq(currentVersion))
                    .execute();

            if (updated == 1) {
                payment.setVersion(currentVersion + 1);
                payment.setUpdatedAt(now);
            }
        }

        for (PaymentHistory history : payment.getHistories()) {
            if (history.getId() != null) {
                continue;
            }

            Long insertedHistoryId = dsl.insertInto(PAYMENT_HISTORIES)
                    .set(PAYMENT_HISTORIES.PAYMENT_ID, payment.getId())
                    .set(PAYMENT_HISTORIES.FROM_STATUS, history.getFromStatus())
                    .set(PAYMENT_HISTORIES.TO_STATUS, history.getToStatus())
                    .set(PAYMENT_HISTORIES.OCCURRED_AT, history.getOccurredAt())
                    .set(PAYMENT_HISTORIES.REASON, history.getReason())
                    .returning(PAYMENT_HISTORIES.ID)
                    .fetchOne(PAYMENT_HISTORIES.ID);

            history.setId(insertedHistoryId);
            history.setPaymentId(payment.getId());
        }

        return payment;
    }

    @Override
    public Payment findById(Long id) {
        if (id == null) {
            return null;
        }

        Payment payment = dsl.selectFrom(PAYMENTS)
                .where(PAYMENTS.ID.eq(id))
                .fetchOne(this::toPayment);
        if (payment == null) {
            return null;
        }

        payment.setHistories(loadHistories(List.of(payment.getId())).getOrDefault(payment.getId(), new ArrayList<>()));
        return payment;
    }

    @Override
    public Payment findByIdempotencyKey(String idempotencyKey) {
        if (idempotencyKey == null) {
            return null;
        }

        Payment payment = dsl.selectFrom(PAYMENTS)
                .where(PAYMENTS.IDEMPOTENCY_KEY.eq(idempotencyKey))
                .fetchOne(this::toPayment);
        if (payment == null) {
            return null;
        }

        payment.setHistories(loadHistories(List.of(payment.getId())).getOrDefault(payment.getId(), new ArrayList<>()));
        return payment;
    }

    @Override
    public List<Payment> findByOrderId(Long orderId) {
        if (orderId == null) {
            return List.of();
        }

        List<Payment> payments = dsl.selectFrom(PAYMENTS)
                .where(PAYMENTS.ORDER_ID.eq(orderId))
                .orderBy(PAYMENTS.ID.desc())
                .fetch(this::toPayment);

        attachHistories(payments);
        return payments;
    }

    private Payment toPayment(Record record) {
        return Payment.builder()
                .id(record.get(PAYMENTS.ID))
                .orderId(record.get(PAYMENTS.ORDER_ID))
                .idempotencyKey(record.get(PAYMENTS.IDEMPOTENCY_KEY))
                .amount(record.get(PAYMENTS.AMOUNT))
                .status(record.get(PAYMENTS.STATUS))
                .paymentKey(record.get(PAYMENTS.PAYMENT_KEY))
                .approvedAt(record.get(PAYMENTS.APPROVED_AT))
                .refundedAt(record.get(PAYMENTS.REFUNDED_AT))
                .version(record.get(PAYMENTS.VERSION))
                .createdAt(record.get(PAYMENTS.CREATED_AT))
                .updatedAt(record.get(PAYMENTS.UPDATED_AT))
                .histories(new ArrayList<>())
                .build();
    }

    private PaymentHistory toPaymentHistory(Record record) {
        return PaymentHistory.builder()
                .id(record.get(PAYMENT_HISTORIES.ID))
                .paymentId(record.get(PAYMENT_HISTORIES.PAYMENT_ID))
                .fromStatus(record.get(PAYMENT_HISTORIES.FROM_STATUS))
                .toStatus(record.get(PAYMENT_HISTORIES.TO_STATUS))
                .occurredAt(record.get(PAYMENT_HISTORIES.OCCURRED_AT))
                .reason(record.get(PAYMENT_HISTORIES.REASON))
                .build();
    }

    private void attachHistories(List<Payment> payments) {
        if (payments.isEmpty()) {
            return;
        }

        List<Long> paymentIds = payments.stream()
                .map(Payment::getId)
                .toList();
        Map<Long, List<PaymentHistory>> historiesByPaymentId = loadHistories(paymentIds);

        for (Payment payment : payments) {
            payment.setHistories(new ArrayList<>(historiesByPaymentId.getOrDefault(payment.getId(), List.of())));
        }
    }

    private Map<Long, List<PaymentHistory>> loadHistories(List<Long> paymentIds) {
        Map<Long, List<PaymentHistory>> historiesByPaymentId = new LinkedHashMap<>();
        if (paymentIds == null || paymentIds.isEmpty()) {
            return historiesByPaymentId;
        }

        dsl.selectFrom(PAYMENT_HISTORIES)
                .where(PAYMENT_HISTORIES.PAYMENT_ID.in(paymentIds))
                .orderBy(PAYMENT_HISTORIES.PAYMENT_ID.asc(), PAYMENT_HISTORIES.ID.asc())
                .fetch()
                .forEach(record -> historiesByPaymentId
                        .computeIfAbsent(record.get(PAYMENT_HISTORIES.PAYMENT_ID), ignored -> new ArrayList<>())
                        .add(toPaymentHistory(record)));

        return historiesByPaymentId;
    }
}
