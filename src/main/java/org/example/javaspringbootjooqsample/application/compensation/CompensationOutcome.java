package org.example.javaspringbootjooqsample.application.compensation;

import java.time.LocalDateTime;

public sealed interface CompensationOutcome
        permits CompensationOutcome.Refunded, CompensationOutcome.Scheduled {

    record Refunded(LocalDateTime refundedAt) implements CompensationOutcome {
    }

    record Scheduled(Long taskId) implements CompensationOutcome {
    }
}
