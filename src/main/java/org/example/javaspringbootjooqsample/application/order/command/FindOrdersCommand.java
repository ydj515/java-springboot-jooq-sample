package org.example.javaspringbootjooqsample.application.order.command;

import lombok.Builder;
import lombok.Getter;
import org.example.javaspringbootjooqsample.domain.order.OrderSearchCriteria;
import org.example.javaspringbootjooqsample.domain.order.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class FindOrdersCommand {
    private final String customerName;
    private final OrderStatus status;
    private final BigDecimal minTotalAmount;
    private final LocalDateTime orderedFrom;
    private final LocalDateTime orderedTo;

    public static FindOrdersCommand empty() {
        return FindOrdersCommand.builder().build();
    }

    public OrderSearchCriteria toCriteria() {
        return OrderSearchCriteria.builder()
                .customerName(customerName)
                .status(status)
                .minTotalAmount(minTotalAmount)
                .orderedFrom(orderedFrom)
                .orderedTo(orderedTo)
                .build();
    }
}
