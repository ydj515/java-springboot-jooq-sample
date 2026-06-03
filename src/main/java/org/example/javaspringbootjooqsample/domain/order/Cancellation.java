package org.example.javaspringbootjooqsample.domain.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.javaspringbootjooqsample.domain.order.exception.IllegalCancellationStateTransitionException;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cancellation {
    private Long id;
    private Long orderId;
    private String idempotencyKey;
    private String reason;
    private CancellationStatus status;
    private LocalDateTime refundedAt;
    private Long version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public void markSucceeded(LocalDateTime refundedAt) {
        ensureTransition(
                CancellationStatus.SUCCEEDED,
                EnumSet.of(CancellationStatus.REQUESTED, CancellationStatus.REFUND_FAILED)
        );
        this.refundedAt = refundedAt;
        this.status = CancellationStatus.SUCCEEDED;
    }

    public void markRefundFailed() {
        ensureTransition(
                CancellationStatus.REFUND_FAILED,
                EnumSet.of(CancellationStatus.REQUESTED)
        );
        this.status = CancellationStatus.REFUND_FAILED;
    }

    private void ensureTransition(CancellationStatus target, Set<CancellationStatus> allowedFrom) {
        if (!allowedFrom.contains(status)) {
            throw new IllegalCancellationStateTransitionException(status, target);
        }
    }

    public static Cancellation requested(Long orderId, String idempotencyKey, String reason) {
        return Cancellation.builder()
                .orderId(orderId)
                .idempotencyKey(idempotencyKey)
                .reason(reason)
                .status(CancellationStatus.REQUESTED)
                .version(0L)
                .build();
    }
}
