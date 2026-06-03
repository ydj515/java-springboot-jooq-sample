package org.example.javaspringbootjooqsample.domain.compensation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompensationTask {
    private static final int LAST_ERROR_MAX_LENGTH = 1000;

    private Long id;
    private CompensationTaskType taskType;
    private String payload;
    private CompensationTaskStatus status;
    private int retryCount;
    private LocalDateTime nextAttemptAt;
    private String lastError;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public void markSuccess(LocalDateTime now) {
        this.status = CompensationTaskStatus.SUCCESS;
        this.lastError = null;
        this.updatedAt = now;
    }

    public void markRetry(String error, LocalDateTime nextAttempt, LocalDateTime now) {
        this.retryCount += 1;
        this.nextAttemptAt = nextAttempt;
        this.lastError = truncate(error);
        this.updatedAt = now;
    }

    public void markFailed(String error, LocalDateTime now) {
        this.status = CompensationTaskStatus.FAILED;
        this.lastError = truncate(error);
        this.updatedAt = now;
    }

    private static String truncate(String error) {
        if (error == null) {
            return null;
        }
        return error.length() <= LAST_ERROR_MAX_LENGTH ? error : error.substring(0, LAST_ERROR_MAX_LENGTH);
    }

    public static CompensationTask pending(
            CompensationTaskType taskType,
            String payload,
            LocalDateTime now
    ) {
        return CompensationTask.builder()
                .taskType(taskType)
                .payload(payload)
                .status(CompensationTaskStatus.PENDING)
                .retryCount(0)
                .nextAttemptAt(now)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }
}
