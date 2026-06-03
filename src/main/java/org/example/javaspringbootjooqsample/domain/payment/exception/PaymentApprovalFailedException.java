package org.example.javaspringbootjooqsample.domain.payment.exception;

import org.example.javaspringbootjooqsample.common.error.BusinessException;
import org.springframework.http.HttpStatus;

public class PaymentApprovalFailedException extends BusinessException {

    public PaymentApprovalFailedException(String message) {
        super(HttpStatus.UNPROCESSABLE_ENTITY, "PAYMENT_APPROVAL_FAILED", message);
    }
}
