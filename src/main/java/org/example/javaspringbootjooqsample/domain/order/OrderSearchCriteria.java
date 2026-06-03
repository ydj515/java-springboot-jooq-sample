package org.example.javaspringbootjooqsample.domain.order;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class OrderSearchCriteria {
    private final String customerName;
    private final OrderStatus status;
    private final BigDecimal minTotalAmount;
    private final LocalDateTime orderedFrom;
    private final LocalDateTime orderedTo;

    public static OrderSearchCriteria empty() {
        return OrderSearchCriteria.builder().build();
    }
}
