package org.example.javaspringbootjooqsample.application.order.result;

import org.example.javaspringbootjooqsample.domain.order.OrderSummary;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderSummaryResult(
        Long id,
        String orderNo,
        String status,
        String customerName,
        long itemCount,
        BigDecimal totalAmount,
        LocalDateTime orderedAt,
        LocalDateTime createdAt
) {
    public static OrderSummaryResult from(OrderSummary summary) {
        return new OrderSummaryResult(
                summary.getId(),
                summary.getOrderNo(),
                summary.getStatus() == null ? null : summary.getStatus().name(),
                summary.getCustomerName(),
                summary.getItemCount(),
                summary.getTotalAmount(),
                summary.getOrderedAt(),
                summary.getCreatedAt()
        );
    }
}
