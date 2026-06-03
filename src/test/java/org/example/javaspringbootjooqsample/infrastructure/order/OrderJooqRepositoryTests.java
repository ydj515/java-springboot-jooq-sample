package org.example.javaspringbootjooqsample.infrastructure.order;

import org.example.javaspringbootjooqsample.application.order.OrderBatchUseCase;
import org.example.javaspringbootjooqsample.application.order.command.AddOrderItemsCommand;
import org.example.javaspringbootjooqsample.application.order.command.OrderItemCommand;
import org.example.javaspringbootjooqsample.application.order.result.AddOrderItemsResult;
import org.example.javaspringbootjooqsample.domain.order.CancelledOrder;
import org.example.javaspringbootjooqsample.domain.order.Order;
import org.example.javaspringbootjooqsample.domain.order.OrderSearchCriteria;
import org.example.javaspringbootjooqsample.domain.order.OrderStatus;
import org.example.javaspringbootjooqsample.domain.order.OrderStatusCount;
import org.example.javaspringbootjooqsample.domain.order.OrderSummary;
import org.example.javaspringbootjooqsample.domain.order.PaidOrder;
import org.example.javaspringbootjooqsample.domain.order.ShippedOrder;
import org.example.javaspringbootjooqsample.domain.order.repository.OrderRepository;
import org.example.javaspringbootjooqsample.domain.order.policy.OrderItemPolicy;
import org.example.javaspringbootjooqsample.domain.order.service.OrderItemAppendService;
import org.example.javaspringbootjooqsample.domain.order.service.OrderLookupService;
import org.example.javaspringbootjooqsample.infrastructure.jooq.reducer.OrderAggregateRowReducer;
import org.example.javaspringbootjooqsample.infrastructure.jooq.support.JooqQueryCounter;
import org.example.javaspringbootjooqsample.support.MySqlJooqRepositoryTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("jooq-repository")
@Import({
        OrderJooqRepositoryAdapter.class,
        OrderSubtypeFactory.class,
        OrderAggregateRowReducer.class,
        OrderLookupService.class,
        OrderItemAppendService.class,
        OrderItemPolicy.class,
        OrderBatchUseCase.class
})
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class OrderJooqRepositoryTests extends MySqlJooqRepositoryTestSupport {

    private final OrderRepository orderRepository;
    private final OrderBatchUseCase orderBatchUseCase;
    private final JooqQueryCounter jooqQueryCounter;

    OrderJooqRepositoryTests(
            OrderRepository orderRepository,
            OrderBatchUseCase orderBatchUseCase,
            JooqQueryCounter jooqQueryCounter
    ) {
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
        // when
        List<Order> joinedOrders = orderRepository.findAll(OrderSearchCriteria.empty());
        int joinQueryCount = jooqQueryCounter.getCount();

        jooqQueryCounter.reset();

        List<Order> nestedSelectOrders = orderRepository.findAllWithNestedSelect(OrderSearchCriteria.empty());
        int nestedSelectQueryCount = jooqQueryCounter.getCount();

        // then
        assertThat(joinedOrders).hasSize(4);
        assertThat(nestedSelectOrders).hasSize(4);
        assertThat(joinQueryCount).isEqualTo(1);
        assertThat(nestedSelectQueryCount).isGreaterThan(joinQueryCount);
    }

    @Test
    void discriminatorMapsOrderSubtypesByStatus() {
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
        Order order = orderRepository.findByIdWithNestedSelect(1L);

        // then
        assertThat(result.addedItemCount()).isEqualTo(2);
        assertThat(result.strategy()).isEqualTo("JOOQ_BATCH");
        assertThat(order.getItems()).hasSize(4);
    }

    @Test
    void sameVersionCompetingUpdatesAllowOnlyOneSuccess() {
        // given
        Order snapshot = orderRepository.findById(1L);
        LocalDateTime now = LocalDateTime.of(2026, 5, 8, 10, 0);

        // when
        int paidUpdated = orderRepository.updateStatusToPaid(
                snapshot.getId(),
                OrderStatus.CREATED,
                snapshot.getVersion(),
                now
        );
        int cancelledUpdated = orderRepository.updateStatusToCancelled(
                snapshot.getId(),
                List.of(OrderStatus.CREATED, OrderStatus.PAID),
                snapshot.getVersion(),
                now.plusMinutes(1)
        );

        // then
        assertThat(paidUpdated + cancelledUpdated).isEqualTo(1);
        assertThat(cancelledUpdated).isZero();

        Order updated = orderRepository.findById(1L);
        assertThat(updated.getStatus()).isEqualTo(OrderStatus.PAID);
        assertThat(updated.getVersion()).isEqualTo(snapshot.getVersion() + 1);
    }

    @Test
    void statusQueriesAndProjectionQueriesReturnExpectedRows() {
        // when
        List<Order> paidOrders = orderRepository.findAllByStatus(OrderStatus.PAID);
        List<OrderSummary> createdSummaries = orderRepository.findOrderSummariesByStatus(OrderStatus.CREATED);
        List<OrderStatusCount> counts = orderRepository.countOrdersByStatus();

        // then
        assertThat(paidOrders).hasSize(1);
        assertThat(paidOrders.getFirst().getStatus()).isEqualTo(OrderStatus.PAID);
        assertThat(createdSummaries).hasSize(1);
        assertThat(createdSummaries.getFirst().getOrderNo()).isEqualTo("ORD-2024-0001");
        assertThat(createdSummaries.getFirst().getCreatedAt()).isNotNull();
        assertThat(counts).extracting(OrderStatusCount::getStatus, OrderStatusCount::getOrderCount)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(OrderStatus.CREATED, 1L),
                        org.assertj.core.groups.Tuple.tuple(OrderStatus.PAID, 1L),
                        org.assertj.core.groups.Tuple.tuple(OrderStatus.SHIPPED, 1L),
                        org.assertj.core.groups.Tuple.tuple(OrderStatus.CANCELLED, 1L)
                );
    }

    @Test
    void bulkShipUpdatesOnlyPaidOrders() {
        // when
        int updatedCount = orderRepository.bulkShipPaidOrders(
                List.of(2L, 3L, 4L),
                LocalDateTime.of(2026, 6, 3, 10, 0)
        );

        // then
        assertThat(updatedCount).isEqualTo(1);
        Order updated = orderRepository.findById(2L);
        assertThat(updated).isInstanceOf(ShippedOrder.class);
        assertThat(updated.getStatus()).isEqualTo(OrderStatus.SHIPPED);
    }
}
