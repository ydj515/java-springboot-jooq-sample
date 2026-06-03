package org.example.javaspringbootjooqsample.presentation.order.response;

import org.example.javaspringbootjooqsample.application.order.result.OrderItemResult;

import java.math.BigDecimal;

public record OrderItemResponse(
        Long id,
        String productName,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal lineAmount
) {
    public static OrderItemResponse from(OrderItemResult result) {
        return new OrderItemResponse(
                result.id(),
                result.productName(),
                result.quantity(),
                result.unitPrice(),
                result.lineAmount()
        );
    }
}
