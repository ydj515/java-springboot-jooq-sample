package org.example.javaspringbootjooqsample.infrastructure.payment.gateway;

import org.example.javaspringbootjooqsample.domain.payment.gateway.ApproveResult;
import org.example.javaspringbootjooqsample.domain.payment.gateway.PaymentGateway;
import org.example.javaspringbootjooqsample.domain.payment.gateway.RefundResult;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class MockPaymentGateway implements PaymentGateway {

    @Override
    public ApproveResult approve(BigDecimal amount, String idempotencyKey) {
        return new ApproveResult(
                "MOCK-PG-" + UUID.randomUUID(),
                LocalDateTime.now()
        );
    }

    @Override
    public RefundResult refund(String paymentKey, BigDecimal amount) {
        return new RefundResult(LocalDateTime.now());
    }
}
