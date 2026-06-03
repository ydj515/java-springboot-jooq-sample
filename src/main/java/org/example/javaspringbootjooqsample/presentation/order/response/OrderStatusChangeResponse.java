package org.example.javaspringbootjooqsample.presentation.order.response;

import org.example.javaspringbootjooqsample.application.order.result.OrderStatusChangeResult;

import java.time.LocalDateTime;

public record OrderStatusChangeResponse(
        Long id,
        String orderNo,
        String status,
        Long version,
        LocalDateTime paidAt,
        LocalDateTime shippedAt,
        LocalDateTime cancelledAt,
        String paymentKey,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static OrderStatusChangeResponse from(OrderStatusChangeResult result) {
        return new OrderStatusChangeResponse(
                result.id(),
                result.orderNo(),
                result.status(),
                result.version(),
                result.paidAt(),
                result.shippedAt(),
                result.cancelledAt(),
                result.paymentKey(),
                result.createdAt(),
                result.updatedAt()
        );
    }
}
