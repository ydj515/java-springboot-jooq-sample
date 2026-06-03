package org.example.javaspringbootjooqsample.domain.order.exception;

import org.example.javaspringbootjooqsample.common.error.BusinessException;
import org.example.javaspringbootjooqsample.domain.order.OrderStatus;
import org.springframework.http.HttpStatus;

import java.util.Collection;

public class InvalidOrderStatusTransitionException extends BusinessException {
    public InvalidOrderStatusTransitionException(
            Long orderId,
            OrderStatus currentStatus,
            OrderStatus targetStatus,
            Collection<OrderStatus> allowedCurrentStatuses
    ) {
        super(
                HttpStatus.CONFLICT,
                "INVALID_ORDER_STATUS_TRANSITION",
                "주문 상태 전이가 허용되지 않습니다. id=%s, current=%s, target=%s, allowedCurrentStatuses=%s"
                        .formatted(orderId, currentStatus, targetStatus, allowedCurrentStatuses)
        );
    }
}
