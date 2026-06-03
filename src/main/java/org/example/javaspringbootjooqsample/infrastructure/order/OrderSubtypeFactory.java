package org.example.javaspringbootjooqsample.infrastructure.order;

import org.example.javaspringbootjooqsample.domain.order.CancelledOrder;
import org.example.javaspringbootjooqsample.domain.order.Order;
import org.example.javaspringbootjooqsample.domain.order.OrderStatus;
import org.example.javaspringbootjooqsample.domain.order.PaidOrder;
import org.example.javaspringbootjooqsample.domain.order.ShippedOrder;
import org.example.javaspringbootjooqsample.generated.jooq_codegen.tables.PurchaseOrders;
import org.jooq.Record;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class OrderSubtypeFactory {

    public Order create(Record record, PurchaseOrders orders) {
        OrderStatus status = record.get(orders.ORDER_STATUS);
        Order order = switch (status) {
            case PAID -> new PaidOrder();
            case SHIPPED -> new ShippedOrder();
            case CANCELLED -> new CancelledOrder();
            case CREATED -> new Order();
            case null -> new Order();
        };

        order.setId(record.get(orders.ID));
        order.setOrderNo(record.get(orders.ORDER_NO));
        order.setStatus(status);
        order.setVersion(record.get(orders.VERSION));
        order.setTotalAmount(record.get(orders.TOTAL_AMOUNT));
        order.setOrderedAt(record.get(orders.ORDERED_AT));
        order.setDeliveryRequestedAt(record.get(orders.DELIVERY_REQUESTED_AT));
        order.setCreatedAt(record.get(orders.CREATED_AT));
        order.setUpdatedAt(record.get(orders.UPDATED_AT));
        order.setItems(new ArrayList<>());

        if (order instanceof PaidOrder paidOrder) {
            paidOrder.setPaidAt(record.get(orders.PAID_AT));
        }
        if (order instanceof ShippedOrder shippedOrder) {
            shippedOrder.setShippedAt(record.get(orders.SHIPPED_AT));
            shippedOrder.setTrackingNumber(record.get(orders.TRACKING_NUMBER));
        }
        if (order instanceof CancelledOrder cancelledOrder) {
            cancelledOrder.setCancelledAt(record.get(orders.CANCELLED_AT));
            cancelledOrder.setCancelReason(record.get(orders.CANCEL_REASON));
        }

        return order;
    }
}
