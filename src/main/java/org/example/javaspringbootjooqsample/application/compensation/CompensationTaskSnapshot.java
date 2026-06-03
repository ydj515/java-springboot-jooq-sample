package org.example.javaspringbootjooqsample.application.compensation;

import org.example.javaspringbootjooqsample.domain.compensation.CompensationTaskStatus;
import org.example.javaspringbootjooqsample.domain.compensation.CompensationTaskType;

public record CompensationTaskSnapshot(
        Long id,
        CompensationTaskType taskType,
        CompensationTaskStatus status,
        int retryCount,
        String payload
) {
}
