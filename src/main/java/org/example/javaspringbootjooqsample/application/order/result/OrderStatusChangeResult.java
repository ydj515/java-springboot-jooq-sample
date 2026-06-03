package org.example.javaspringbootjooqsample.application.order.result;

import org.example.javaspringbootjooqsample.domain.order.CancelledOrder;
import org.example.javaspringbootjooqsample.domain.order.Order;
import org.example.javaspringbootjooqsample.domain.order.PaidOrder;
import org.example.javaspringbootjooqsample.domain.order.ShippedOrder;

import java.time.LocalDateTime;

public record OrderStatusChangeResult(
        Long id,
        String orderNo,
        String status,
        Long version,
        LocalDateTime paidAt,
        LocalDateTime shippedAt,
        LocalDateTime cancelledAt,
        String paymentKey,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static OrderStatusChangeResult from(Order order) {
        return from(order, null);
    }

    public static OrderStatusChangeResult from(Order order, String paymentKey) {
        return new OrderStatusChangeResult(
                order.getId(),
                order.getOrderNo(),
                order.getStatus() == null ? null : order.getStatus().name(),
                order.getVersion(),
                extractPaidAt(order),
                extractShippedAt(order),
                extractCancelledAt(order),
                paymentKey,
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }

    private static LocalDateTime extractPaidAt(Order order) {
        if (order instanceof PaidOrder paidOrder) {
            return paidOrder.getPaidAt();
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
}
