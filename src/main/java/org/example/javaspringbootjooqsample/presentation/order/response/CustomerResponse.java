package org.example.javaspringbootjooqsample.presentation.order.response;

import org.example.javaspringbootjooqsample.application.order.result.CustomerResult;

public record CustomerResponse(
        Long id,
        String name,
        String email
) {
    public static CustomerResponse from(CustomerResult result) {
        if (result == null) {
            return null;
        }

        return new CustomerResponse(
                result.id(),
                result.name(),
                result.email()
        );
    }
}
