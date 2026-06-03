package org.example.javaspringbootjooqsample.domain.order.repository;

import org.example.javaspringbootjooqsample.domain.order.Order;
import org.example.javaspringbootjooqsample.domain.order.OrderItem;
import org.example.javaspringbootjooqsample.domain.order.OrderSearchCriteria;
import org.example.javaspringbootjooqsample.domain.order.OrderStatus;
import org.example.javaspringbootjooqsample.domain.order.OrderStatusCount;
import org.example.javaspringbootjooqsample.domain.order.OrderSummary;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository {
    List<Order> findAll(OrderSearchCriteria criteria);

    List<Order> findAllWithNestedSelect(OrderSearchCriteria criteria);

    Order findById(Long id);

    Order findByIdForUpdate(Long id);

    default Optional<Order> findOptionalById(Long id) {
        return Optional.ofNullable(findById(id));
    }

    Order findByIdWithNestedSelect(Long id);

    List<OrderSummary> findSummaries(OrderSearchCriteria criteria);

    List<Order> findAllByStatus(OrderStatus status);

    List<OrderSummary> findOrderSummariesByStatus(OrderStatus status);

    List<OrderStatusCount> countOrdersByStatus();

    int updateStatusToPaid(Long orderId, OrderStatus currentStatus, Long version, LocalDateTime paidAt);

    int updateStatusToCancelled(
            Long orderId,
            List<OrderStatus> cancellableStatuses,
            Long version,
            LocalDateTime cancelledAt
    );

    int updateStatusToShipped(Long orderId, OrderStatus currentStatus, Long version, LocalDateTime shippedAt);

    int bulkShipPaidOrders(List<Long> orderIds, LocalDateTime shippedAt);

    int insertOrderItem(Long orderId, OrderItem item);

    int insertOrderItems(Long orderId, List<OrderItem> items);

    int save(Order order);
}
