package org.example.javaspringbootjooqsample.domain.order.exception;

import org.example.javaspringbootjooqsample.common.error.BusinessException;
import org.springframework.http.HttpStatus;

public class OrderIdRequiredException extends BusinessException {
    public OrderIdRequiredException() {
        super(HttpStatus.BAD_REQUEST, "ORDER_ID_REQUIRED", "주문 식별자(id)는 필수입니다.");
    }
}
