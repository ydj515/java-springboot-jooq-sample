package org.example.javaspringbootjooqsample.presentation.order.request;

import lombok.Getter;
import lombok.Setter;
import org.example.javaspringbootjooqsample.application.order.command.CreateOrderCommand;
import org.example.javaspringbootjooqsample.application.order.command.OrderItemCommand;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class CreateOrderRequest {
    private Long customerId;
    private LocalDateTime deliveryRequestedAt;
    private List<OrderItemRequest> items;

    public CreateOrderCommand toCommand() {
        List<OrderItemCommand> itemCommands = items == null
                ? List.of()
                : items.stream().map(OrderItemRequest::toCommand).toList();

        return CreateOrderCommand.builder()
                .customerId(customerId)
                .deliveryRequestedAt(deliveryRequestedAt)
                .items(itemCommands)
                .build();
    }
}
