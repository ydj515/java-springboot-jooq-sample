package org.example.javaspringbootjooqsample.domain.order.exception;

import org.example.javaspringbootjooqsample.common.error.BusinessException;
import org.springframework.http.HttpStatus;

public class InvalidOrderItemException extends BusinessException {
    public InvalidOrderItemException(String message) {
        super(HttpStatus.BAD_REQUEST, "ORDER_ITEM_INVALID", message);
    }
}
