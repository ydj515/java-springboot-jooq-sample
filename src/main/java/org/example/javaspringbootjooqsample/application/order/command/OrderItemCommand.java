package org.example.javaspringbootjooqsample.application.order.command;

import lombok.Builder;
import org.example.javaspringbootjooqsample.domain.order.OrderItem;

import java.math.BigDecimal;

@Builder
public record OrderItemCommand(
        String productName,
        int quantity,
        BigDecimal unitPrice
) {
    public OrderItem toDomain() {
        return OrderItem.of(productName, quantity, unitPrice);
    }
}
