package org.example.javaspringbootjooqsample.domain.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.example.javaspringbootjooqsample.domain.customer.Customer;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Order {
    private Long id;
    private String orderNo;
    private OrderStatus status;
    private Long version;
    private BigDecimal totalAmount;
    private LocalDateTime orderedAt;
    private LocalDateTime deliveryRequestedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Customer customer;
    private List<OrderItem> items;
}
