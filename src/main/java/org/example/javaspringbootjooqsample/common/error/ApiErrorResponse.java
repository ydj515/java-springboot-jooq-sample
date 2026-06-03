package org.example.javaspringbootjooqsample.common.error;

import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

public record ApiErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String code,
        String message,
        String path,
        String traceId
) {
    public static ApiErrorResponse from(HttpStatus status, String code, String message, String path, String traceId) {
        return new ApiErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                code,
                message,
                path,
                traceId
        );
    }
}
