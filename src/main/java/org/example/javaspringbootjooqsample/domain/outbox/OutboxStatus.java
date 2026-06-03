package org.example.javaspringbootjooqsample.domain.outbox;

public enum OutboxStatus {
    PENDING,
    PUBLISHED,
    FAILED
}
