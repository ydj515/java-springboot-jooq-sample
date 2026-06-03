package org.example.javaspringbootjooqsample.application.order.result;

import org.example.javaspringbootjooqsample.domain.order.OrderStatusCount;

public record OrderStatusCountResult(
        String status,
        long orderCount
) {
    public static OrderStatusCountResult from(OrderStatusCount count) {
        return new OrderStatusCountResult(
                count.getStatus() == null ? null : count.getStatus().name(),
                count.getOrderCount()
        );
    }
}
