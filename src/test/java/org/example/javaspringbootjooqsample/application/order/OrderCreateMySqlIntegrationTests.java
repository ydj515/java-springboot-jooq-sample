package org.example.javaspringbootjooqsample.application.order;

import org.example.javaspringbootjooqsample.application.order.command.CreateOrderCommand;
import org.example.javaspringbootjooqsample.application.order.command.OrderItemCommand;
import org.example.javaspringbootjooqsample.application.order.result.OrderResult;
import org.example.javaspringbootjooqsample.support.MySqlIntegrationTestSupport;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("integration-test")
@SpringBootTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class OrderCreateMySqlIntegrationTests extends MySqlIntegrationTestSupport {

    private final OrderUseCase orderUseCase;

    OrderCreateMySqlIntegrationTests(OrderUseCase orderUseCase) {
        this.orderUseCase = orderUseCase;
    }

    @Test
    void createOrderGeneratesOrderNoAndPersistsOrderWithItems() {
        CreateOrderCommand command = CreateOrderCommand.builder()
                .customerId(1L)
                .deliveryRequestedAt(LocalDateTime.of(2024, 8, 1, 18, 0))
                .items(List.of(
                        OrderItemCommand.builder()
                                .productName("Mechanical Keyboard")
                                .quantity(1)
                                .unitPrice(new BigDecimal("129000"))
                                .build(),
                        OrderItemCommand.builder()
                                .productName("Wrist Rest")
                                .quantity(2)
                                .unitPrice(new BigDecimal("20000"))
                                .build()
                ))
                .build();

        OrderResult result = orderUseCase.createOrder(command);

        assertThat(result.id()).isNotNull();
        assertThat(result.orderNo()).matches("ORD-\\d{4}-\\d{2}-\\d{2}-[0-9A-F]{10}");
        assertThat(result.status()).isEqualTo("CREATED");
        assertThat(result.version()).isEqualTo(0L);
        assertThat(result.totalAmount()).isEqualByComparingTo("169000");
        assertThat(result.deliveryRequestedAt()).isEqualTo(LocalDateTime.of(2024, 8, 1, 18, 0));
        assertThat(result.customer().id()).isEqualTo(1L);
        assertThat(result.items()).hasSize(2);
    }

    @Test
    void createOrderTwiceProducesDistinctOrderNos() {
        CreateOrderCommand command1 = CreateOrderCommand.builder()
                .customerId(1L)
                .items(List.of(OrderItemCommand.builder()
                        .productName("A")
                        .quantity(1)
                        .unitPrice(new BigDecimal("1000"))
                        .build()))
                .build();
        CreateOrderCommand command2 = CreateOrderCommand.builder()
                .customerId(2L)
                .items(List.of(OrderItemCommand.builder()
                        .productName("B")
                        .quantity(1)
                        .unitPrice(new BigDecimal("2000"))
                        .build()))
                .build();

        OrderResult first = orderUseCase.createOrder(command1);
        OrderResult second = orderUseCase.createOrder(command2);

        assertThat(first.orderNo()).isNotEqualTo(second.orderNo());
    }
}
