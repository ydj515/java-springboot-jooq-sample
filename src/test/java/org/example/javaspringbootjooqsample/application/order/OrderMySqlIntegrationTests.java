package org.example.javaspringbootjooqsample.application.order;

import org.example.javaspringbootjooqsample.application.order.command.AddOrderItemsCommand;
import org.example.javaspringbootjooqsample.application.order.command.FindOrdersCommand;
import org.example.javaspringbootjooqsample.application.order.command.GetOrderCommand;
import org.example.javaspringbootjooqsample.application.order.command.OrderItemCommand;
import org.example.javaspringbootjooqsample.application.order.result.AddOrderItemsResult;
import org.example.javaspringbootjooqsample.application.order.result.OrderResult;
import org.example.javaspringbootjooqsample.domain.order.CancelledOrder;
import org.example.javaspringbootjooqsample.domain.order.Order;
import org.example.javaspringbootjooqsample.domain.order.PaidOrder;
import org.example.javaspringbootjooqsample.domain.order.ShippedOrder;
import org.example.javaspringbootjooqsample.domain.order.repository.OrderRepository;
import org.example.javaspringbootjooqsample.infrastructure.jooq.support.JooqQueryCounter;
import org.example.javaspringbootjooqsample.support.MySqlIntegrationTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestConstructor;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("integration-test")
@SpringBootTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class OrderMySqlIntegrationTests extends MySqlIntegrationTestSupport {

    private final OrderUseCase orderUseCase;

    private final OrderRepository orderRepository;

    private final OrderBatchUseCase orderBatchUseCase;

    private final JooqQueryCounter jooqQueryCounter;

    OrderMySqlIntegrationTests(
            OrderUseCase orderUseCase,
            OrderRepository orderRepository,
            OrderBatchUseCase orderBatchUseCase,
            JooqQueryCounter jooqQueryCounter
    ) {
        this.orderUseCase = orderUseCase;
        this.orderRepository = orderRepository;
        this.orderBatchUseCase = orderBatchUseCase;
        this.jooqQueryCounter = jooqQueryCounter;
    }

    @BeforeEach
    void setUp() {
        jooqQueryCounter.reset();
    }

    @Test
    void joinBasedQueryUsesFewerSqlThanNestedSelect() {
        // given
        FindOrdersCommand command = FindOrdersCommand.empty();

        // when
        List<OrderResult> joinedOrders = orderUseCase.getOrders(command);
        int joinQueryCount = jooqQueryCounter.getCount();

        jooqQueryCounter.reset();

        List<OrderResult> nestedSelectOrders = orderUseCase.getOrdersWithNestedSelect(command);
        int nestedSelectQueryCount = jooqQueryCounter.getCount();

        // then
        assertThat(joinedOrders).hasSize(4);
        assertThat(nestedSelectOrders).hasSize(4);
        assertThat(joinQueryCount).isEqualTo(1);
        assertThat(nestedSelectQueryCount).isGreaterThan(joinQueryCount);
    }

    @Test
    void discriminatorMapsOrderSubtypesByStatus() {
        // given
        // 상태별 주문 데이터가 MySQL Testcontainers에 적재되어 있습니다.

        // when
        Order paidOrder = orderRepository.findById(2L);
        Order shippedOrder = orderRepository.findById(3L);
        Order cancelledOrder = orderRepository.findById(4L);

        // then
        assertThat(paidOrder).isInstanceOf(PaidOrder.class);
        assertThat(shippedOrder).isInstanceOf(ShippedOrder.class);
        assertThat(cancelledOrder).isInstanceOf(CancelledOrder.class);
    }

    @Test
    void batchSessionInsertsMultipleItems() {
        // given
        AddOrderItemsResult result = orderBatchUseCase.addOrderItemsWithBatchSession(
                AddOrderItemsCommand.builder()
                        .orderId(1L)
                        .items(List.of(
                                OrderItemCommand.builder()
                                        .productName("USB Hub")
                                        .quantity(1)
                                        .unitPrice(new BigDecimal("29000"))
                                        .build(),
                                OrderItemCommand.builder()
                                        .productName("Laptop Stand")
                                        .quantity(1)
                                        .unitPrice(new BigDecimal("49000"))
                                        .build()
                        ))
                        .build()
        );

        // when
        OrderResult order = orderUseCase.getOrderWithNestedSelect(new GetOrderCommand(1L));

        // then
        assertThat(result.addedItemCount()).isEqualTo(2);
        assertThat(result.strategy()).isEqualTo("JOOQ_BATCH");
        assertThat(order.items()).hasSize(4);
    }
}
