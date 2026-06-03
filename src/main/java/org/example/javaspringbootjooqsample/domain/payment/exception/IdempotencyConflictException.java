package org.example.javaspringbootjooqsample.domain.payment.exception;

import org.example.javaspringbootjooqsample.common.error.BusinessException;
import org.springframework.http.HttpStatus;

public class IdempotencyConflictException extends BusinessException {

    public IdempotencyConflictException(String message) {
        super(HttpStatus.CONFLICT, "IDEMPOTENCY_KEY_CONFLICT", message);
    }
}
