package org.example.javaspringbootjooqsample.application.order;

import org.example.javaspringbootjooqsample.application.order.result.OrderStatusChangeResult;

import java.math.BigDecimal;

public sealed interface CancelOrderPreparation
        permits CancelOrderPreparation.Replay, CancelOrderPreparation.Completed, CancelOrderPreparation.RefundRequired {

    record Replay(OrderStatusChangeResult result) implements CancelOrderPreparation {
    }

    record Completed(OrderStatusChangeResult result) implements CancelOrderPreparation {
    }

    record RefundRequired(
            OrderStatusChangeResult result,
            Long cancellationId,
            Long paymentId,
            String paymentKey,
            BigDecimal amount,
            String reason
    ) implements CancelOrderPreparation {
    }
}
