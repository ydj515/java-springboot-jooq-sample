package org.example.javaspringbootjooqsample.infrastructure.order;

import org.example.javaspringbootjooqsample.domain.order.OrderSearchCriteria;
import org.example.javaspringbootjooqsample.generated.jooq_codegen.tables.Customers;
import org.example.javaspringbootjooqsample.generated.jooq_codegen.tables.PurchaseOrders;
import org.jooq.Condition;
import org.jooq.impl.DSL;
import org.springframework.util.StringUtils;

final class OrderConditions {

    private OrderConditions() {
    }

    static Condition search(OrderSearchCriteria criteria, PurchaseOrders orders, Customers customers) {
        if (criteria == null) {
            return DSL.noCondition();
        }

        Condition condition = DSL.noCondition();

        if (StringUtils.hasText(criteria.getCustomerName())) {
            condition = condition.and(customers.NAME.like(criteria.getCustomerName() + "%"));
        }
        if (criteria.getStatus() != null) {
            condition = condition.and(orders.ORDER_STATUS.eq(criteria.getStatus()));
        }
        if (criteria.getMinTotalAmount() != null) {
            condition = condition.and(orders.TOTAL_AMOUNT.ge(criteria.getMinTotalAmount()));
        }
        if (criteria.getOrderedFrom() != null) {
            condition = condition.and(orders.ORDERED_AT.ge(criteria.getOrderedFrom()));
        }
        if (criteria.getOrderedTo() != null) {
            condition = condition.and(orders.ORDERED_AT.le(criteria.getOrderedTo()));
        }

        return condition;
    }
}
