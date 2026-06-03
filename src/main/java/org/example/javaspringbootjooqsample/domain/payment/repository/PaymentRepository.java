package org.example.javaspringbootjooqsample.domain.payment.repository;

import org.example.javaspringbootjooqsample.domain.payment.Payment;

import java.util.List;

public interface PaymentRepository {
    Payment save(Payment payment);

    Payment findById(Long id);

    Payment findByIdempotencyKey(String idempotencyKey);

    List<Payment> findByOrderId(Long orderId);
}
