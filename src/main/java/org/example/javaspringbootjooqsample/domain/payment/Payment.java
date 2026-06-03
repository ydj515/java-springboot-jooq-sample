package org.example.javaspringbootjooqsample.domain.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.javaspringbootjooqsample.domain.payment.exception.IllegalPaymentStateTransitionException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {
    private Long id;
    private Long orderId;
    private String idempotencyKey;
    private BigDecimal amount;
    private PaymentStatus status;
    private String paymentKey;
    private LocalDateTime approvedAt;
    private LocalDateTime refundedAt;
    private Long version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Builder.Default
    private List<PaymentHistory> histories = new ArrayList<>();

    public List<PaymentHistory> getHistories() {
        return Collections.unmodifiableList(histories);
    }

    public void markApproved(String paymentKey, LocalDateTime approvedAt, String reason) {
        ensureTransition(PaymentStatus.APPROVED, EnumSet.of(PaymentStatus.REQUESTED));
        this.paymentKey = paymentKey;
        this.approvedAt = approvedAt;
        recordTransition(PaymentStatus.APPROVED, approvedAt, reason);
    }

    public void markApproved(String paymentKey, LocalDateTime approvedAt) {
        markApproved(paymentKey, approvedAt, "PG approved");
    }

    public void markFailed(String reason, LocalDateTime occurredAt) {
        ensureTransition(PaymentStatus.FAILED, EnumSet.of(PaymentStatus.REQUESTED));
        recordTransition(PaymentStatus.FAILED, occurredAt, reason);
    }

    public void markRefunded(LocalDateTime refundedAt, String reason) {
        ensureTransition(PaymentStatus.REFUNDED, EnumSet.of(PaymentStatus.APPROVED, PaymentStatus.REFUND_FAILED));
        this.refundedAt = refundedAt;
        recordTransition(PaymentStatus.REFUNDED, refundedAt, reason);
    }

    public void markRefundFailed(String reason, LocalDateTime occurredAt) {
        ensureTransition(PaymentStatus.REFUND_FAILED, EnumSet.of(PaymentStatus.APPROVED));
        recordTransition(PaymentStatus.REFUND_FAILED, occurredAt, reason);
    }

    private void ensureTransition(PaymentStatus target, Set<PaymentStatus> allowedFrom) {
        if (!allowedFrom.contains(status)) {
            throw new IllegalPaymentStateTransitionException(status, target);
        }
    }

    private void recordTransition(PaymentStatus target, LocalDateTime occurredAt, String reason) {
        PaymentStatus previous = status;
        this.status = target;
        histories.add(PaymentHistory.of(previous, target, occurredAt, reason));
    }

    public static Payment request(
            Long orderId,
            String idempotencyKey,
            BigDecimal amount,
            LocalDateTime requestedAt
    ) {
        Payment payment = Payment.builder()
                .orderId(orderId)
                .idempotencyKey(idempotencyKey)
                .amount(amount)
                .status(PaymentStatus.REQUESTED)
                .version(0L)
                .histories(new ArrayList<>())
                .build();
        payment.histories.add(
                PaymentHistory.of(null, PaymentStatus.REQUESTED, requestedAt, "payment requested")
        );
        return payment;
    }
}
