package org.example.javaspringbootjooqsample.presentation.order.response;

import org.example.javaspringbootjooqsample.application.order.result.OrderResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        String orderNo,
        String status,
        Long version,
        BigDecimal totalAmount,
        LocalDateTime orderedAt,
        LocalDateTime deliveryRequestedAt,
        LocalDateTime shippedAt,
        LocalDateTime cancelledAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime paidAt,
        String trackingNumber,
        String cancelReason,
        CustomerResponse customer,
        List<OrderItemResponse> items
) {
    public static OrderResponse from(OrderResult result) {
        return new OrderResponse(
                result.id(),
                result.orderNo(),
                result.status(),
                result.version(),
                result.totalAmount(),
                result.orderedAt(),
                result.deliveryRequestedAt(),
                result.shippedAt(),
                result.cancelledAt(),
                result.createdAt(),
                result.updatedAt(),
                result.paidAt(),
                result.trackingNumber(),
                result.cancelReason(),
                CustomerResponse.from(result.customer()),
                result.items().stream()
                        .map(OrderItemResponse::from)
                        .toList()
        );
    }
}
