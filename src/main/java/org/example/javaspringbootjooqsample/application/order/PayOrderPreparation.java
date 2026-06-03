package org.example.javaspringbootjooqsample.application.order;

import org.example.javaspringbootjooqsample.application.order.result.OrderStatusChangeResult;

import java.math.BigDecimal;

public sealed interface PayOrderPreparation
        permits PayOrderPreparation.Replay, PayOrderPreparation.ApprovalRequired {

    record Replay(OrderStatusChangeResult result) implements PayOrderPreparation {
    }

    record ApprovalRequired(
            Long orderId,
            Long paymentId,
            BigDecimal amount,
            String idempotencyKey
    ) implements PayOrderPreparation {
    }
}
