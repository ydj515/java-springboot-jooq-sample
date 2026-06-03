package org.example.javaspringbootjooqsample.domain.payment.exception;

import org.example.javaspringbootjooqsample.common.error.BusinessException;
import org.springframework.http.HttpStatus;

public class IdempotencyKeyRequiredException extends BusinessException {

    public IdempotencyKeyRequiredException() {
        super(HttpStatus.BAD_REQUEST, "IDEMPOTENCY_KEY_REQUIRED", "Idempotency-Key header is required");
    }
}
