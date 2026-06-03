package org.example.javaspringbootjooqsample.domain.payment.gateway;

import java.time.LocalDateTime;

public record ApproveResult(
        String paymentKey,
        LocalDateTime approvedAt
) {
}
