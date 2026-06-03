package org.example.javaspringbootjooqsample.domain.payment.exception;

import org.example.javaspringbootjooqsample.common.error.BusinessException;
import org.springframework.http.HttpStatus;

public class IdempotencyKeyInvalidException extends BusinessException {

    public IdempotencyKeyInvalidException(String message) {
        super(HttpStatus.BAD_REQUEST, "IDEMPOTENCY_KEY_INVALID", message);
    }
}
