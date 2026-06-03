package org.example.javaspringbootjooqsample.infrastructure.jooq.reducer;

import org.example.javaspringbootjooqsample.domain.customer.Customer;
import org.example.javaspringbootjooqsample.domain.order.Order;
import org.example.javaspringbootjooqsample.domain.order.OrderItem;
import org.example.javaspringbootjooqsample.generated.jooq_codegen.tables.Customers;
import org.example.javaspringbootjooqsample.generated.jooq_codegen.tables.PurchaseOrderItems;
import org.example.javaspringbootjooqsample.generated.jooq_codegen.tables.PurchaseOrders;
import org.example.javaspringbootjooqsample.infrastructure.order.OrderSubtypeFactory;
import org.jooq.Record;
import org.jooq.Result;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class OrderAggregateRowReducer {

    private final OrderSubtypeFactory subtypeFactory;

    public OrderAggregateRowReducer(OrderSubtypeFactory subtypeFactory) {
        this.subtypeFactory = subtypeFactory;
    }

    public List<Order> reduce(
            Result<? extends Record> records,
            PurchaseOrders orders,
            Customers customers,
            PurchaseOrderItems items
    ) {
        Map<Long, OrderAggregate> aggregates = new LinkedHashMap<>();

        for (Record record : records) {
            Long orderId = record.get(orders.ID);
            if (orderId == null) {
                continue;
            }

            OrderAggregate aggregate = aggregates.computeIfAbsent(
                    orderId,
                    ignored -> new OrderAggregate(subtypeFactory.create(record, orders))
            );

            if (aggregate.order.getCustomer() == null) {
                Long customerId = record.get(customers.ID);
                if (customerId != null) {
                    aggregate.order.setCustomer(toCustomer(record, customers));
                }
            }

            Long itemId = record.get(items.ID);
            if (itemId == null || aggregate.itemsById.containsKey(itemId)) {
                continue;
            }

            aggregate.itemsById.put(itemId, toOrderItem(record, items));
        }

        return aggregates.values().stream()
                .map(OrderAggregate::toOrder)
                .toList();
    }

    private Customer toCustomer(Record record, Customers customers) {
        return Customer.builder()
                .id(record.get(customers.ID))
                .name(record.get(customers.NAME))
                .email(record.get(customers.EMAIL))
                .createdAt(record.get(customers.CREATED_AT))
                .updatedAt(record.get(customers.UPDATED_AT))
                .build();
    }

    private OrderItem toOrderItem(Record record, PurchaseOrderItems items) {
        return OrderItem.builder()
                .id(record.get(items.ID))
                .productName(record.get(items.PRODUCT_NAME))
                .quantity(record.get(items.QUANTITY))
                .unitPrice(record.get(items.UNIT_PRICE))
                .lineAmount(record.get(items.LINE_AMOUNT))
                .build();
    }

    private static final class OrderAggregate {
        private final Order order;
        private final Map<Long, OrderItem> itemsById = new LinkedHashMap<>();

        private OrderAggregate(Order order) {
            this.order = order;
        }

        private Order toOrder() {
            order.setItems(new ArrayList<>(itemsById.values()));
            return order;
        }
    }
}
