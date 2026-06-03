package org.example.javaspringbootjooqsample.domain.order;

import lombok.Getter;
import lombok.ToString;

@Getter
    @ToString
public class OrderStatusCount {
    private final OrderStatus status;
    private final long orderCount;

    public OrderStatusCount(OrderStatus status, Long orderCount) {
        this.status = status;
        this.orderCount = orderCount == null ? 0L : orderCount;
    }
}
