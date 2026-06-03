package org.example.javaspringbootjooqsample.presentation.order.request;

import lombok.Getter;
import lombok.Setter;
import org.example.javaspringbootjooqsample.application.order.command.OrderItemCommand;

import java.math.BigDecimal;

@Getter
@Setter
public class OrderItemRequest {
    private String productName;
    private int quantity;
    private BigDecimal unitPrice;

    public OrderItemCommand toCommand() {
        return OrderItemCommand.builder()
                .productName(productName)
                .quantity(quantity)
                .unitPrice(unitPrice)
                .build();
    }
}
