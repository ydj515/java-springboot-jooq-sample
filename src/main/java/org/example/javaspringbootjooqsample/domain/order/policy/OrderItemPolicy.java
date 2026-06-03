package org.example.javaspringbootjooqsample.domain.order.policy;

import org.example.javaspringbootjooqsample.domain.order.OrderItem;
import org.example.javaspringbootjooqsample.domain.order.exception.InvalidOrderItemException;
import org.example.javaspringbootjooqsample.domain.order.exception.OrderItemsRequiredException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;

@Component
public class OrderItemPolicy {

    public void validateBatchItems(List<OrderItem> items) {
        if (items == null || items.isEmpty()) {
            throw new OrderItemsRequiredException();
        }

        for (int index = 0; index < items.size(); index++) {
            validateOrderItem(items.get(index), index + 1);
        }
    }

    private void validateOrderItem(OrderItem item, int itemNumber) {
        if (item == null) {
            throw new InvalidOrderItemException("주문 상품 " + itemNumber + "번은 null일 수 없습니다.");
        }

        if (!StringUtils.hasText(item.getProductName())) {
            throw new InvalidOrderItemException("주문 상품 " + itemNumber + "번의 productName은 필수입니다.");
        }

        if (item.getQuantity() <= 0) {
            throw new InvalidOrderItemException("주문 상품 " + itemNumber + "번의 quantity는 1 이상이어야 합니다.");
        }

        if (item.getUnitPrice() == null || item.getUnitPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidOrderItemException("주문 상품 " + itemNumber + "번의 unitPrice는 0보다 커야 합니다.");
        }

        if (item.getLineAmount() == null || item.getLineAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidOrderItemException("주문 상품 " + itemNumber + "번의 lineAmount는 0보다 커야 합니다.");
        }

        BigDecimal expectedLineAmount = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
        if (expectedLineAmount.compareTo(item.getLineAmount()) != 0) {
            throw new InvalidOrderItemException(
                    "주문 상품 " + itemNumber + "번의 lineAmount는 quantity * unitPrice와 같아야 합니다."
            );
        }
    }
}
