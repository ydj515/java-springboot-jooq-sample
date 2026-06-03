package org.example.javaspringbootjooqsample.domain.order.exception;

import org.example.javaspringbootjooqsample.common.error.BusinessException;
import org.example.javaspringbootjooqsample.domain.order.CancellationStatus;
import org.springframework.http.HttpStatus;

public class IllegalCancellationStateTransitionException extends BusinessException {

    public IllegalCancellationStateTransitionException(CancellationStatus from, CancellationStatus to) {
        super(
                HttpStatus.CONFLICT,
                "ILLEGAL_CANCELLATION_STATE_TRANSITION",
                "cannot transit cancellation from " + from + " to " + to
        );
    }
}
