package org.example.javaspringbootjooqsample.application.order.command;

public record CancelOrderCommand(Long orderId, String idempotencyKey, String reason) {
}
