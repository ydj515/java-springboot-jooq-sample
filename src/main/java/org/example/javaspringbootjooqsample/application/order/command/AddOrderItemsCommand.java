package org.example.javaspringbootjooqsample.application.order.command;

import lombok.Builder;
import lombok.Getter;
import org.example.javaspringbootjooqsample.domain.order.OrderItem;

import java.util.List;

@Getter
@Builder
public class AddOrderItemsCommand {
    private final Long orderId;
    private final List<OrderItemCommand> items;

    public List<OrderItem> toDomainItems() {
        if (items == null || items.isEmpty()) {
            return List.of();
        }

        return items.stream()
                .map(OrderItemCommand::toDomain)
                .toList();
    }
}
