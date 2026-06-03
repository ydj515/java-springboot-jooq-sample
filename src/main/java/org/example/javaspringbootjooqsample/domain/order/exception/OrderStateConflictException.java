package org.example.javaspringbootjooqsample.domain.order.exception;

import org.example.javaspringbootjooqsample.common.error.BusinessException;
import org.example.javaspringbootjooqsample.domain.order.OrderStatus;
import org.springframework.http.HttpStatus;

public class OrderStateConflictException extends BusinessException {
    public OrderStateConflictException(Long orderId, OrderStatus currentStatus, OrderStatus targetStatus, Long version) {
        super(
                HttpStatus.CONFLICT,
                "ORDER_STATE_CONFLICT",
                "주문 상태 변경 중 충돌이 발생했습니다. id=%s, current=%s, target=%s, version=%s"
                        .formatted(orderId, currentStatus, targetStatus, version)
        );
    }
}
