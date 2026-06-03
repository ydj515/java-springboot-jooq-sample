package org.example.javaspringbootjooqsample.application.order.result;

import org.example.javaspringbootjooqsample.domain.order.OrderItem;

import java.math.BigDecimal;

public record OrderItemResult(
        Long id,
        String productName,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal lineAmount
) {
    public static OrderItemResult from(OrderItem item) {
        return new OrderItemResult(
                item.getId(),
                item.getProductName(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getLineAmount()
        );
    }
}
