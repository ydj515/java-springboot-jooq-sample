package org.example.javaspringbootjooqsample.domain.order.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderPaidEvent(
        String eventId,
        Long orderId,
        Long paymentId,
        String paymentKey,
        BigDecimal amount,
        LocalDateTime paidAt
) {
    public static final String EVENT_TYPE = "OrderPaidEvent";
    public static final String AGGREGATE_TYPE = "Order";
}
