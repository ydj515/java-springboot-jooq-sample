package org.example.javaspringbootjooqsample.presentation.customer.response;

import org.example.javaspringbootjooqsample.application.customer.result.CustomerResult;

import java.time.LocalDateTime;

public record CustomerResponse(
        Long id,
        String name,
        String email,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static CustomerResponse from(CustomerResult result) {
        return new CustomerResponse(
                result.id(),
                result.name(),
                result.email(),
                result.createdAt(),
                result.updatedAt()
        );
    }
}
