package org.example.javaspringbootjooqsample.infrastructure.order;

import lombok.RequiredArgsConstructor;
import org.example.javaspringbootjooqsample.domain.customer.Customer;
import org.example.javaspringbootjooqsample.domain.order.Order;
import org.example.javaspringbootjooqsample.domain.order.OrderItem;
import org.example.javaspringbootjooqsample.domain.order.OrderSearchCriteria;
import org.example.javaspringbootjooqsample.domain.order.OrderStatus;
import org.example.javaspringbootjooqsample.domain.order.OrderStatusCount;
import org.example.javaspringbootjooqsample.domain.order.OrderSummary;
import org.example.javaspringbootjooqsample.domain.order.repository.OrderRepository;
import org.example.javaspringbootjooqsample.generated.jooq_codegen.tables.Customers;
import org.example.javaspringbootjooqsample.generated.jooq_codegen.tables.PurchaseOrderItems;
import org.example.javaspringbootjooqsample.generated.jooq_codegen.tables.PurchaseOrders;
import org.example.javaspringbootjooqsample.generated.jooq_codegen.tables.records.PurchaseOrderItemsRecord;
import org.example.javaspringbootjooqsample.generated.jooq_codegen.tables.records.PurchaseOrdersRecord;
import org.example.javaspringbootjooqsample.infrastructure.jooq.reducer.OrderAggregateRowReducer;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.InsertValuesStep5;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SelectConditionStep;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.example.javaspringbootjooqsample.generated.jooq_codegen.tables.Customers.CUSTOMERS;
import static org.example.javaspringbootjooqsample.generated.jooq_codegen.tables.PurchaseOrderItems.PURCHASE_ORDER_ITEMS;
import static org.example.javaspringbootjooqsample.generated.jooq_codegen.tables.PurchaseOrders.PURCHASE_ORDERS;

@Repository
@RequiredArgsConstructor
public class OrderJooqRepositoryAdapter implements OrderRepository {

    private final DSLContext dsl;
    private final OrderAggregateRowReducer reducer;
    private final OrderSubtypeFactory subtypeFactory;

    @Override
    public List<Order> findAll(OrderSearchCriteria criteria) {
        PurchaseOrders orders = PURCHASE_ORDERS.as("po");
        Customers customers = CUSTOMERS.as("c");
        PurchaseOrderItems items = PURCHASE_ORDER_ITEMS.as("poi");

        Result<Record> records = selectOrdersWithCustomerAndItems(orders, customers, items)
                .and(OrderConditions.search(criteria, orders, customers))
                .orderBy(orders.ID.desc(), items.ID.asc())
                .fetch();

        return reducer.reduce(records, orders, customers, items);
    }

    @Override
    public List<Order> findAllWithNestedSelect(OrderSearchCriteria criteria) {
        PurchaseOrders orders = PURCHASE_ORDERS.as("po");
        Customers customers = CUSTOMERS.as("c");

        return dsl.select(OrderSelects.nestedSelectFields(orders))
                .from(orders)
                .join(customers).on(customers.ID.eq(orders.CUSTOMER_ID))
                .where(OrderConditions.search(criteria, orders, customers))
                .orderBy(orders.ID.desc())
                .fetch(record -> toNestedSelectOrder(record, orders))
                .stream()
                .map(this::hydrateCustomerAndItems)
                .toList();
    }

    @Override
    public Order findById(Long id) {
        if (id == null) {
            return null;
        }

        PurchaseOrders orders = PURCHASE_ORDERS.as("po");
        Customers customers = CUSTOMERS.as("c");
        PurchaseOrderItems items = PURCHASE_ORDER_ITEMS.as("poi");

        return reducer.reduce(
                        selectOrdersWithCustomerAndItems(orders, customers, items)
                                .and(orders.ID.eq(id))
                                .orderBy(orders.ID.desc(), items.ID.asc())
                                .fetch(),
                        orders,
                        customers,
                        items
                )
                .stream()
                .findFirst()
                .orElse(null);
    }

    @Override
    public Order findByIdForUpdate(Long id) {
        if (id == null) {
            return null;
        }

        PurchaseOrders orders = PURCHASE_ORDERS.as("po");
        Customers customers = CUSTOMERS.as("c");
        PurchaseOrderItems items = PURCHASE_ORDER_ITEMS.as("poi");

        return reducer.reduce(
                        selectOrdersWithCustomerAndItems(orders, customers, items)
                                .and(orders.ID.eq(id))
                                .orderBy(orders.ID.desc(), items.ID.asc())
                                .forUpdate()
                                .fetch(),
                        orders,
                        customers,
                        items
                )
                .stream()
                .findFirst()
                .orElse(null);
    }

    @Override
    public Order findByIdWithNestedSelect(Long id) {
        if (id == null) {
            return null;
        }

        PurchaseOrders orders = PURCHASE_ORDERS.as("po");

        Order order = dsl.select(OrderSelects.nestedSelectFields(orders))
                .from(orders)
                .where(orders.ID.eq(id))
                .fetchOne(record -> toNestedSelectOrder(record, orders));

        if (order == null) {
            return null;
        }

        return hydrateCustomerAndItems(order);
    }

    @Override
    public List<OrderSummary> findSummaries(OrderSearchCriteria criteria) {
        PurchaseOrders orders = PURCHASE_ORDERS.as("po");
        Customers customers = CUSTOMERS.as("c");
        PurchaseOrderItems items = PURCHASE_ORDER_ITEMS.as("poi");
        Field<Long> itemCount = DSL.count(items.ID).cast(Long.class).as("item_count");

        return dsl.select(OrderSelects.summaryFields(orders, customers, itemCount))
                .from(orders)
                .join(customers).on(customers.ID.eq(orders.CUSTOMER_ID))
                .leftJoin(items).on(items.ORDER_ID.eq(orders.ID))
                .where(OrderConditions.search(criteria, orders, customers))
                .groupBy(
                        orders.ID,
                        orders.ORDER_NO,
                        orders.ORDER_STATUS,
                        customers.NAME,
                        orders.TOTAL_AMOUNT,
                        orders.ORDERED_AT,
                        orders.CREATED_AT
                )
                .orderBy(orders.ID.desc())
                .fetch(record -> toOrderSummary(record, orders, customers, itemCount));
    }

    @Override
    public List<Order> findAllByStatus(OrderStatus status) {
        PurchaseOrders orders = PURCHASE_ORDERS.as("po");
        Customers customers = CUSTOMERS.as("c");
        PurchaseOrderItems items = PURCHASE_ORDER_ITEMS.as("poi");

        return reducer.reduce(
                selectOrdersWithCustomerAndItems(orders, customers, items)
                        .and(orders.ORDER_STATUS.eq(status))
                        .orderBy(orders.ID.desc(), items.ID.asc())
                        .fetch(),
                orders,
                customers,
                items
        );
    }

    @Override
    public List<OrderSummary> findOrderSummariesByStatus(OrderStatus status) {
        PurchaseOrders orders = PURCHASE_ORDERS.as("po");
        Customers customers = CUSTOMERS.as("c");
        PurchaseOrderItems items = PURCHASE_ORDER_ITEMS.as("poi");
        Field<Long> itemCount = DSL.count(items.ID).cast(Long.class).as("item_count");

        return dsl.select(OrderSelects.summaryFields(orders, customers, itemCount))
                .from(orders)
                .join(customers).on(customers.ID.eq(orders.CUSTOMER_ID))
                .leftJoin(items).on(items.ORDER_ID.eq(orders.ID))
                .where(orders.ORDER_STATUS.eq(status))
                .groupBy(
                        orders.ID,
                        orders.ORDER_NO,
                        orders.ORDER_STATUS,
                        customers.NAME,
                        orders.TOTAL_AMOUNT,
                        orders.ORDERED_AT,
                        orders.CREATED_AT
                )
                .orderBy(orders.CREATED_AT.desc(), orders.ID.desc())
                .fetch(record -> toOrderSummary(record, orders, customers, itemCount));
    }

    @Override
    public List<OrderStatusCount> countOrdersByStatus() {
        Field<Long> orderCount = DSL.count().cast(Long.class).as("order_count");
        Field<Integer> statusOrder = DSL.when(PURCHASE_ORDERS.ORDER_STATUS.eq(OrderStatus.CREATED), 1)
                .when(PURCHASE_ORDERS.ORDER_STATUS.eq(OrderStatus.PAID), 2)
                .when(PURCHASE_ORDERS.ORDER_STATUS.eq(OrderStatus.SHIPPED), 3)
                .when(PURCHASE_ORDERS.ORDER_STATUS.eq(OrderStatus.CANCELLED), 4)
                .otherwise(99);

        return dsl.select(PURCHASE_ORDERS.ORDER_STATUS, orderCount)
                .from(PURCHASE_ORDERS)
                .groupBy(PURCHASE_ORDERS.ORDER_STATUS)
                .orderBy(statusOrder.asc())
                .fetch(record -> new OrderStatusCount(
                        record.get(PURCHASE_ORDERS.ORDER_STATUS),
                        record.get(orderCount)
                ));
    }

    @Override
    public int updateStatusToPaid(Long orderId, OrderStatus currentStatus, Long version, LocalDateTime paidAt) {
        return dsl.update(PURCHASE_ORDERS)
                .set(PURCHASE_ORDERS.ORDER_STATUS, OrderStatus.PAID)
                .set(PURCHASE_ORDERS.VERSION, PURCHASE_ORDERS.VERSION.plus(1L))
                .set(PURCHASE_ORDERS.PAID_AT, paidAt)
                .set(PURCHASE_ORDERS.UPDATED_AT, DSL.currentLocalDateTime())
                .where(PURCHASE_ORDERS.ID.eq(orderId))
                .and(PURCHASE_ORDERS.ORDER_STATUS.eq(currentStatus))
                .and(PURCHASE_ORDERS.VERSION.eq(version))
                .execute();
    }

    @Override
    public int updateStatusToCancelled(
            Long orderId,
            List<OrderStatus> cancellableStatuses,
            Long version,
            LocalDateTime cancelledAt
    ) {
        if (cancellableStatuses == null || cancellableStatuses.isEmpty()) {
            return 0;
        }

        return dsl.update(PURCHASE_ORDERS)
                .set(PURCHASE_ORDERS.ORDER_STATUS, OrderStatus.CANCELLED)
                .set(PURCHASE_ORDERS.VERSION, PURCHASE_ORDERS.VERSION.plus(1L))
                .set(PURCHASE_ORDERS.CANCELLED_AT, cancelledAt)
                .set(PURCHASE_ORDERS.UPDATED_AT, DSL.currentLocalDateTime())
                .where(PURCHASE_ORDERS.ID.eq(orderId))
                .and(PURCHASE_ORDERS.ORDER_STATUS.in(cancellableStatuses))
                .and(PURCHASE_ORDERS.VERSION.eq(version))
                .execute();
    }

    @Override
    public int updateStatusToShipped(Long orderId, OrderStatus currentStatus, Long version, LocalDateTime shippedAt) {
        return dsl.update(PURCHASE_ORDERS)
                .set(PURCHASE_ORDERS.ORDER_STATUS, OrderStatus.SHIPPED)
                .set(PURCHASE_ORDERS.VERSION, PURCHASE_ORDERS.VERSION.plus(1L))
                .set(PURCHASE_ORDERS.SHIPPED_AT, shippedAt)
                .set(PURCHASE_ORDERS.UPDATED_AT, DSL.currentLocalDateTime())
                .where(PURCHASE_ORDERS.ID.eq(orderId))
                .and(PURCHASE_ORDERS.ORDER_STATUS.eq(currentStatus))
                .and(PURCHASE_ORDERS.VERSION.eq(version))
                .execute();
    }

    @Override
    public int bulkShipPaidOrders(List<Long> orderIds, LocalDateTime shippedAt) {
        if (orderIds == null || orderIds.isEmpty()) {
            return 0;
        }

        return dsl.update(PURCHASE_ORDERS)
                .set(PURCHASE_ORDERS.ORDER_STATUS, OrderStatus.SHIPPED)
                .set(PURCHASE_ORDERS.SHIPPED_AT, shippedAt)
                .set(PURCHASE_ORDERS.UPDATED_AT, DSL.currentLocalDateTime())
                .set(PURCHASE_ORDERS.VERSION, PURCHASE_ORDERS.VERSION.plus(1L))
                .where(PURCHASE_ORDERS.ORDER_STATUS.eq(OrderStatus.PAID))
                .and(PURCHASE_ORDERS.ID.in(orderIds))
                .execute();
    }

    @Override
    public int insertOrderItem(Long orderId, OrderItem item) {
        return dsl.insertInto(PURCHASE_ORDER_ITEMS)
                .set(PURCHASE_ORDER_ITEMS.ORDER_ID, orderId)
                .set(PURCHASE_ORDER_ITEMS.PRODUCT_NAME, item.getProductName())
                .set(PURCHASE_ORDER_ITEMS.QUANTITY, item.getQuantity())
                .set(PURCHASE_ORDER_ITEMS.UNIT_PRICE, item.getUnitPrice())
                .set(PURCHASE_ORDER_ITEMS.LINE_AMOUNT, item.getLineAmount())
                .execute();
    }

    @Override
    public int insertOrderItems(Long orderId, List<OrderItem> items) {
        if (items == null || items.isEmpty()) {
            return 0;
        }

        InsertValuesStep5<PurchaseOrderItemsRecord, Long, String, Integer, BigDecimal, BigDecimal> insertStep =
                dsl.insertInto(
                        PURCHASE_ORDER_ITEMS,
                        PURCHASE_ORDER_ITEMS.ORDER_ID,
                        PURCHASE_ORDER_ITEMS.PRODUCT_NAME,
                        PURCHASE_ORDER_ITEMS.QUANTITY,
                        PURCHASE_ORDER_ITEMS.UNIT_PRICE,
                        PURCHASE_ORDER_ITEMS.LINE_AMOUNT
                );

        for (OrderItem item : items) {
            insertStep = insertStep.values(
                    orderId,
                    item.getProductName(),
                    item.getQuantity(),
                    item.getUnitPrice(),
                    item.getLineAmount()
            );
        }

        return insertStep.execute();
    }

    @Override
    public int save(Order order) {
        PurchaseOrdersRecord inserted = dsl.insertInto(PURCHASE_ORDERS)
                .set(PURCHASE_ORDERS.CUSTOMER_ID, order.getCustomer().getId())
                .set(PURCHASE_ORDERS.ORDER_NO, order.getOrderNo())
                .set(PURCHASE_ORDERS.ORDER_STATUS, order.getStatus())
                .set(PURCHASE_ORDERS.VERSION, order.getVersion())
                .set(PURCHASE_ORDERS.TOTAL_AMOUNT, order.getTotalAmount())
                .set(PURCHASE_ORDERS.ORDERED_AT, order.getOrderedAt())
                .set(PURCHASE_ORDERS.DELIVERY_REQUESTED_AT, order.getDeliveryRequestedAt())
                .returning(PURCHASE_ORDERS.ID)
                .fetchOne();

        if (inserted == null) {
            return 0;
        }

        order.setId(inserted.getId());
        return 1;
    }

    private SelectConditionStep<Record> selectOrdersWithCustomerAndItems(
            PurchaseOrders orders,
            Customers customers,
            PurchaseOrderItems items
    ) {
        return dsl.select(OrderSelects.detailFields(orders, customers, items))
                .from(orders)
                .join(customers).on(customers.ID.eq(orders.CUSTOMER_ID))
                .leftJoin(items).on(items.ORDER_ID.eq(orders.ID))
                .where(DSL.noCondition());
    }

    private Order toNestedSelectOrder(Record record, PurchaseOrders orders) {
        Order order = subtypeFactory.create(record, orders);
        order.setCustomer(Customer.builder().id(record.get(orders.CUSTOMER_ID)).build());
        return order;
    }

    private Order hydrateCustomerAndItems(Order order) {
        Long customerId = order.getCustomer() == null ? null : order.getCustomer().getId();
        if (customerId != null) {
            order.setCustomer(loadCustomer(customerId));
        }
        order.setItems(loadItems(order.getId()));
        return order;
    }

    private Customer loadCustomer(Long customerId) {
        return dsl.select(
                        CUSTOMERS.ID,
                        CUSTOMERS.NAME,
                        CUSTOMERS.EMAIL,
                        CUSTOMERS.CREATED_AT,
                        CUSTOMERS.UPDATED_AT
                )
                .from(CUSTOMERS)
                .where(CUSTOMERS.ID.eq(customerId))
                .fetchOne(record -> Customer.builder()
                        .id(record.get(CUSTOMERS.ID))
                        .name(record.get(CUSTOMERS.NAME))
                        .email(record.get(CUSTOMERS.EMAIL))
                        .createdAt(record.get(CUSTOMERS.CREATED_AT))
                        .updatedAt(record.get(CUSTOMERS.UPDATED_AT))
                        .build());
    }

    private List<OrderItem> loadItems(Long orderId) {
        return dsl.select(
                        PURCHASE_ORDER_ITEMS.ID,
                        PURCHASE_ORDER_ITEMS.PRODUCT_NAME,
                        PURCHASE_ORDER_ITEMS.QUANTITY,
                        PURCHASE_ORDER_ITEMS.UNIT_PRICE,
                        PURCHASE_ORDER_ITEMS.LINE_AMOUNT
                )
                .from(PURCHASE_ORDER_ITEMS)
                .where(PURCHASE_ORDER_ITEMS.ORDER_ID.eq(orderId))
                .orderBy(PURCHASE_ORDER_ITEMS.ID.asc())
                .fetch(record -> OrderItem.builder()
                        .id(record.get(PURCHASE_ORDER_ITEMS.ID))
                        .productName(record.get(PURCHASE_ORDER_ITEMS.PRODUCT_NAME))
                        .quantity(record.get(PURCHASE_ORDER_ITEMS.QUANTITY))
                        .unitPrice(record.get(PURCHASE_ORDER_ITEMS.UNIT_PRICE))
                        .lineAmount(record.get(PURCHASE_ORDER_ITEMS.LINE_AMOUNT))
                        .build());
    }

    private OrderSummary toOrderSummary(
            Record record,
            PurchaseOrders orders,
            Customers customers,
            Field<Long> itemCount
    ) {
        return new OrderSummary(
                record.get(orders.ID),
                record.get(orders.ORDER_NO),
                record.get(orders.ORDER_STATUS),
                record.get(customers.NAME),
                record.get(itemCount),
                record.get(orders.TOTAL_AMOUNT),
                record.get(orders.ORDERED_AT),
                record.get(orders.CREATED_AT)
        );
    }
}
