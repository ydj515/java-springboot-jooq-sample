package org.example.javaspringbootjooqsample.domain.order.policy;

import org.example.javaspringbootjooqsample.domain.order.Order;
import org.example.javaspringbootjooqsample.domain.order.OrderStatus;
import org.example.javaspringbootjooqsample.domain.order.exception.InvalidOrderStatusTransitionException;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderStatusTransitionPolicy {
    private static final List<OrderStatus> PAYABLE_STATUSES = List.of(OrderStatus.CREATED);
    private static final List<OrderStatus> CANCELLABLE_STATUSES = List.of(OrderStatus.CREATED, OrderStatus.PAID);
    private static final List<OrderStatus> SHIPPABLE_STATUSES = List.of(OrderStatus.PAID);

    public void validatePayable(Order order) {
        validate(order, OrderStatus.PAID, PAYABLE_STATUSES);
    }

    public void validateCancellable(Order order) {
        validate(order, OrderStatus.CANCELLED, CANCELLABLE_STATUSES);
    }

    public void validateShippable(Order order) {
        validate(order, OrderStatus.SHIPPED, SHIPPABLE_STATUSES);
    }

    private void validate(Order order, OrderStatus targetStatus, List<OrderStatus> allowedCurrentStatuses) {
        OrderStatus currentStatus = order == null ? null : order.getStatus();
        if (order == null || !allowedCurrentStatuses.contains(currentStatus)) {
            throw new InvalidOrderStatusTransitionException(
                    order == null ? null : order.getId(),
                    currentStatus,
                    targetStatus,
                    allowedCurrentStatuses
            );
        }
    }
}
