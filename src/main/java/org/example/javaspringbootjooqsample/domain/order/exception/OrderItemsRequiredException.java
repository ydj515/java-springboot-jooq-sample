package org.example.javaspringbootjooqsample.domain.order.exception;

import org.example.javaspringbootjooqsample.common.error.BusinessException;
import org.springframework.http.HttpStatus;

public class OrderItemsRequiredException extends BusinessException {
    public OrderItemsRequiredException() {
        super(HttpStatus.BAD_REQUEST, "ORDER_ITEMS_REQUIRED", "추가할 주문 상품 목록은 비어 있을 수 없습니다.");
    }
}
