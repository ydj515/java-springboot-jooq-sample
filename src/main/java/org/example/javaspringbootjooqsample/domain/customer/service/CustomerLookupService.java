package org.example.javaspringbootjooqsample.domain.customer.service;

import lombok.RequiredArgsConstructor;
import org.example.javaspringbootjooqsample.domain.customer.exception.CustomerNotFoundException;
import org.example.javaspringbootjooqsample.domain.customer.repository.CustomerRepository;
import org.example.javaspringbootjooqsample.domain.customer.Customer;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomerLookupService {

    private final CustomerRepository customerRepository;

    public Customer requireById(Long id) {
        if (id == null) {
            throw new CustomerNotFoundException(null);
        }

        Customer customer = customerRepository.findById(id);
        if (customer == null) {
            throw new CustomerNotFoundException(id);
        }

        return customer;
    }
}
