package org.example.javaspringbootjooqsample.presentation.order.response;

import org.example.javaspringbootjooqsample.application.order.result.OrderStatusCountResult;

public record OrderStatusCountResponse(
        String status,
        long orderCount
) {
    public static OrderStatusCountResponse from(OrderStatusCountResult result) {
        return new OrderStatusCountResponse(result.status(), result.orderCount());
    }
}
