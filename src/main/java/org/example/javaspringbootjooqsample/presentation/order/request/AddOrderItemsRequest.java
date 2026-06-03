package org.example.javaspringbootjooqsample.presentation.order.request;

import lombok.Getter;
import lombok.Setter;
import org.example.javaspringbootjooqsample.application.order.command.AddOrderItemsCommand;
import org.example.javaspringbootjooqsample.application.order.command.OrderItemCommand;

import java.util.List;

@Getter
@Setter
public class AddOrderItemsRequest {
    private List<OrderItemRequest> items;

    public AddOrderItemsCommand toCommand(Long orderId) {
        return AddOrderItemsCommand.builder()
                .orderId(orderId)
                .items(toItemCommands())
                .build();
    }

    private List<OrderItemCommand> toItemCommands() {
        if (items == null || items.isEmpty()) {
            return List.of();
        }

        return items.stream()
                .map(OrderItemRequest::toCommand)
                .toList();
    }
}
