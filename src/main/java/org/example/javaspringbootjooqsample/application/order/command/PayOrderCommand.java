package org.example.javaspringbootjooqsample.application.order.command;

public record PayOrderCommand(Long orderId, String idempotencyKey) {
}
