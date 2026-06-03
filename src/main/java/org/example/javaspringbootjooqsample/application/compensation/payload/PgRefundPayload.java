package org.example.javaspringbootjooqsample.application.compensation.payload;

import java.math.BigDecimal;

public record PgRefundPayload(
        Long paymentId,
        String paymentKey,
        BigDecimal amount,
        String reason
) {
}
