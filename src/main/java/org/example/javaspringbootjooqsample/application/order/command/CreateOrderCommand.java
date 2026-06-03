package org.example.javaspringbootjooqsample.application.order.command;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record CreateOrderCommand(
        Long customerId,
        LocalDateTime deliveryRequestedAt,
        List<OrderItemCommand> items
) {
    public List<org.example.javaspringbootjooqsample.domain.order.OrderItem> toDomainItems() {
        return items == null
                ? List.of()
                : items.stream().map(OrderItemCommand::toDomain).toList();
    }
}
