package org.example.javaspringbootjooqsample.presentation.order.request;

import lombok.Getter;
import lombok.Setter;
import org.example.javaspringbootjooqsample.application.order.command.FindOrdersCommand;
import org.example.javaspringbootjooqsample.common.search.BaseSearchParam;
import org.example.javaspringbootjooqsample.domain.order.OrderStatus;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class OrderSearchRequest extends BaseSearchParam {
    private String customerName;
    private OrderStatus status;
    private BigDecimal minTotalAmount;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime orderedFrom;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime orderedTo;

    public FindOrdersCommand toCommand() {
        return FindOrdersCommand.builder()
                .customerName(customerName)
                .status(status)
                .minTotalAmount(minTotalAmount)
                .orderedFrom(orderedFrom)
                .orderedTo(orderedTo)
                .build();
    }
}
