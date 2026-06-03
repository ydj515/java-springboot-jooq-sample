package org.example.javaspringbootjooqsample.domain.outbox;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OutboxEvent {
    private static final int LAST_ERROR_MAX_LENGTH = 1000;

    private Long id;
    private String eventId;
    private String aggregateType;
    private String aggregateId;
    private String eventType;
    private String payload;
    private OutboxStatus status;
    private int retryCount;
    private LocalDateTime nextAttemptAt;
    private LocalDateTime publishedAt;
    private String lastError;
    private LocalDateTime createdAt;

    public void markPublished(LocalDateTime publishedAt) {
        this.status = OutboxStatus.PUBLISHED;
        this.publishedAt = publishedAt;
        this.lastError = null;
    }

    public void markRetry(String error, LocalDateTime nextAttempt) {
        this.retryCount += 1;
        this.nextAttemptAt = nextAttempt;
        this.lastError = truncate(error);
    }

    public void markFailed(String error) {
        this.status = OutboxStatus.FAILED;
        this.lastError = truncate(error);
    }

    private static String truncate(String error) {
        if (error == null) {
            return null;
        }
        return error.length() <= LAST_ERROR_MAX_LENGTH ? error : error.substring(0, LAST_ERROR_MAX_LENGTH);
    }

    public static OutboxEvent pending(
            String aggregateType,
            String aggregateId,
            String eventType,
            String payload,
            LocalDateTime now
    ) {
        return OutboxEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .aggregateType(aggregateType)
                .aggregateId(aggregateId)
                .eventType(eventType)
                .payload(payload)
                .status(OutboxStatus.PENDING)
                .retryCount(0)
                .nextAttemptAt(now)
                .createdAt(now)
                .build();
    }
}
