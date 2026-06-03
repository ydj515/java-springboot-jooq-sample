package org.example.javaspringbootjooqsample.application.customer.result;

import org.example.javaspringbootjooqsample.domain.customer.Customer;

import java.time.LocalDateTime;

public record CustomerResult(
        Long id,
        String name,
        String email,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static CustomerResult from(Customer customer) {
        return new CustomerResult(
                customer.getId(),
                customer.getName(),
                customer.getEmail(),
                customer.getCreatedAt(),
                customer.getUpdatedAt()
        );
    }
}
