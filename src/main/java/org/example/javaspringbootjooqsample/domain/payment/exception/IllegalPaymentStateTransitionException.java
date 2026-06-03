package org.example.javaspringbootjooqsample.domain.payment.exception;

import org.example.javaspringbootjooqsample.common.error.BusinessException;
import org.example.javaspringbootjooqsample.domain.payment.PaymentStatus;
import org.springframework.http.HttpStatus;

public class IllegalPaymentStateTransitionException extends BusinessException {

    public IllegalPaymentStateTransitionException(PaymentStatus from, PaymentStatus to) {
        super(
                HttpStatus.CONFLICT,
                "ILLEGAL_PAYMENT_STATE_TRANSITION",
                "cannot transit payment from " + from + " to " + to
        );
    }
}
