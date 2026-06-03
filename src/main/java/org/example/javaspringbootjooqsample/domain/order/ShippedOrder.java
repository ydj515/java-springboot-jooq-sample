package org.example.javaspringbootjooqsample.domain.order;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
public class ShippedOrder extends PaidOrder {
    private LocalDateTime shippedAt;
    private String trackingNumber;
}
