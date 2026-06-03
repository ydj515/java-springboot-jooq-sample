package org.example.javaspringbootjooqsample.domain.payment.exception;

import org.example.javaspringbootjooqsample.common.error.BusinessException;
import org.springframework.http.HttpStatus;

public class PaymentNotFoundException extends BusinessException {

    public PaymentNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, "PAYMENT_NOT_FOUND", message);
    }
}
