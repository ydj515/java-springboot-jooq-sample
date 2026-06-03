package org.example.javaspringbootjooqsample.domain.order.repository;

import org.example.javaspringbootjooqsample.domain.order.Cancellation;

import java.util.List;

public interface CancellationRepository {
    Cancellation save(Cancellation cancellation);

    Cancellation findById(Long id);

    Cancellation findByIdempotencyKey(String idempotencyKey);

    List<Cancellation> findByOrderId(Long orderId);
}
