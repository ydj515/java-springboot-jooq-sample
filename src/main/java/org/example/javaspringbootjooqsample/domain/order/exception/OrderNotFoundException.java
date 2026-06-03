package org.example.javaspringbootjooqsample.domain.order.exception;

import org.example.javaspringbootjooqsample.common.error.BusinessException;
import org.springframework.http.HttpStatus;

public class OrderNotFoundException extends BusinessException {
    public OrderNotFoundException(Long id) {
        super(HttpStatus.NOT_FOUND, "ORDER_NOT_FOUND", "주문을 찾을 수 없습니다. id=" + id);
    }
}
