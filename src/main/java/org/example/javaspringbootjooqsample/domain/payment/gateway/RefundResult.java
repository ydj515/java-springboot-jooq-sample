package org.example.javaspringbootjooqsample.domain.payment.gateway;

import java.time.LocalDateTime;

public record RefundResult(
        LocalDateTime refundedAt
) {
}
