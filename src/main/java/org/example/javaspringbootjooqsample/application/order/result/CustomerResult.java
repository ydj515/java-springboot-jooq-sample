package org.example.javaspringbootjooqsample.application.order.result;

import org.example.javaspringbootjooqsample.domain.customer.Customer;

public record CustomerResult(
        Long id,
        String name,
        String email
) {
    public static CustomerResult from(Customer customer) {
        if (customer == null) {
            return null;
        }

        return new CustomerResult(
                customer.getId(),
                customer.getName(),
                customer.getEmail()
        );
    }
}
