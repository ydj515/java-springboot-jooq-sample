package org.example.javaspringbootjooqsample.domain.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {
    private Long id;
    private String productName;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal lineAmount;

    public static OrderItem of(
            String productName,
            int quantity,
            BigDecimal unitPrice
    ) {
        BigDecimal computedLineAmount = unitPrice == null
                ? BigDecimal.ZERO
                : unitPrice.multiply(BigDecimal.valueOf(quantity));

        return OrderItem.builder()
                .productName(productName)
                .quantity(quantity)
                .unitPrice(unitPrice)
                .lineAmount(computedLineAmount)
                .build();
    }
}
