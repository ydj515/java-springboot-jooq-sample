package org.example.javaspringbootjooqsample.application.order.result;

import org.example.javaspringbootjooqsample.domain.order.CancelledOrder;
import org.example.javaspringbootjooqsample.domain.order.Order;
import org.example.javaspringbootjooqsample.domain.order.PaidOrder;
import org.example.javaspringbootjooqsample.domain.order.ShippedOrder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResult(
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
        CustomerResult customer,
        List<OrderItemResult> items
) {
    public static OrderResult from(Order order) {
        return new OrderResult(
                order.getId(),
                order.getOrderNo(),
                order.getStatus() == null ? null : order.getStatus().name(),
                order.getVersion(),
                order.getTotalAmount(),
                order.getOrderedAt(),
                order.getDeliveryRequestedAt(),
                extractShippedAt(order),
                extractCancelledAt(order),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                extractPaidAt(order),
                extractTrackingNumber(order),
                extractCancelReason(order),
                CustomerResult.from(order.getCustomer()),
                mapItems(order)
        );
    }

    private static List<OrderItemResult> mapItems(Order order) {
        if (order.getItems() == null || order.getItems().isEmpty()) {
            return List.of();
        }

        return order.getItems().stream()
                .map(OrderItemResult::from)
                .toList();
    }

    private static LocalDateTime extractPaidAt(Order order) {
        if (order instanceof PaidOrder paidOrder) {
            return paidOrder.getPaidAt();
        }

        return null;
    }

    private static String extractTrackingNumber(Order order) {
        if (order instanceof ShippedOrder shippedOrder) {
            return shippedOrder.getTrackingNumber();
        }

        return null;
    }

    private static LocalDateTime extractShippedAt(Order order) {
        if (order instanceof ShippedOrder shippedOrder) {
            return shippedOrder.getShippedAt();
        }

        return null;
    }

    private static LocalDateTime extractCancelledAt(Order order) {
        if (order instanceof CancelledOrder cancelledOrder) {
            return cancelledOrder.getCancelledAt();
        }

        return null;
    }

    private static String extractCancelReason(Order order) {
        if (order instanceof CancelledOrder cancelledOrder) {
            return cancelledOrder.getCancelReason();
        }

        return null;
    }
}
