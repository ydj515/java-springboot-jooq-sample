package org.example.javaspringbootjooqsample.infrastructure.order;

import org.example.javaspringbootjooqsample.generated.jooq_codegen.tables.Customers;
import org.example.javaspringbootjooqsample.generated.jooq_codegen.tables.PurchaseOrderItems;
import org.example.javaspringbootjooqsample.generated.jooq_codegen.tables.PurchaseOrders;
import org.jooq.Field;
import org.jooq.SelectField;

final class OrderSelects {

    private OrderSelects() {
    }

    static SelectField<?>[] detailFields(
            PurchaseOrders orders,
            Customers customers,
            PurchaseOrderItems items
    ) {
        return new SelectField<?>[]{
                orders.ID,
                orders.CUSTOMER_ID,
                orders.ORDER_NO,
                orders.ORDER_STATUS,
                orders.VERSION,
                orders.TOTAL_AMOUNT,
                orders.ORDERED_AT,
                orders.DELIVERY_REQUESTED_AT,
                orders.PAID_AT,
                orders.SHIPPED_AT,
                orders.CANCELLED_AT,
                orders.CREATED_AT,
                orders.UPDATED_AT,
                orders.TRACKING_NUMBER,
                orders.CANCEL_REASON,
                customers.ID,
                customers.NAME,
                customers.EMAIL,
                customers.CREATED_AT,
                customers.UPDATED_AT,
                items.ID,
                items.PRODUCT_NAME,
                items.QUANTITY,
                items.UNIT_PRICE,
                items.LINE_AMOUNT
        };
    }

    static SelectField<?>[] nestedSelectFields(PurchaseOrders orders) {
        return new SelectField<?>[]{
                orders.ID,
                orders.CUSTOMER_ID,
                orders.ORDER_NO,
                orders.ORDER_STATUS,
                orders.VERSION,
                orders.TOTAL_AMOUNT,
                orders.ORDERED_AT,
                orders.DELIVERY_REQUESTED_AT,
                orders.PAID_AT,
                orders.SHIPPED_AT,
                orders.CANCELLED_AT,
                orders.CREATED_AT,
                orders.UPDATED_AT,
                orders.TRACKING_NUMBER,
                orders.CANCEL_REASON
        };
    }

    static SelectField<?>[] summaryFields(
            PurchaseOrders orders,
            Customers customers,
            Field<Long> itemCount
    ) {
        return new SelectField<?>[]{
                orders.ID,
                orders.ORDER_NO,
                orders.ORDER_STATUS,
                customers.NAME,
                itemCount,
                orders.TOTAL_AMOUNT,
                orders.ORDERED_AT,
                orders.CREATED_AT
        };
    }
}
