package org.example.javaspringbootjooqsample.presentation.order.response;

import org.example.javaspringbootjooqsample.application.order.result.OrderSummaryResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderSummaryResponse(
        Long id,
        String orderNo,
        String status,
        String customerName,
        long itemCount,
        BigDecimal totalAmount,
        LocalDateTime orderedAt,
        LocalDateTime createdAt
) {
    public static OrderSummaryResponse from(OrderSummaryResult result) {
        return new OrderSummaryResponse(
                result.id(),
                result.orderNo(),
                result.status(),
                result.customerName(),
                result.itemCount(),
                result.totalAmount(),
                result.orderedAt(),
                result.createdAt()
        );
    }
}
