package org.example.javaspringbootjooqsample.domain.order;

import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@ToString
public class OrderSummary {
    private final Long id;
    private final String orderNo;
    private final OrderStatus status;
    private final String customerName;
    private final long itemCount;
    private final BigDecimal totalAmount;
    private final LocalDateTime orderedAt;
    private final LocalDateTime createdAt;

    public OrderSummary(
            Long id,
            String orderNo,
            OrderStatus status,
            String customerName,
            Long itemCount,
            BigDecimal totalAmount,
            LocalDateTime orderedAt,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.orderNo = orderNo;
        this.status = status;
        this.customerName = customerName;
        this.itemCount = itemCount == null ? 0L : itemCount;
        this.totalAmount = totalAmount;
        this.orderedAt = orderedAt;
        this.createdAt = createdAt;
    }
}
