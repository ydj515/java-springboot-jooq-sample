package org.example.javaspringbootjooqsample.domain.payment.gateway;

import java.math.BigDecimal;

public interface PaymentGateway {
    ApproveResult approve(BigDecimal amount, String idempotencyKey);

    RefundResult refund(String paymentKey, BigDecimal amount);
}
