package org.example.javaspringbootjooqsample.domain.customer.repository;

import org.example.javaspringbootjooqsample.domain.customer.Customer;

import java.util.List;

public interface CustomerRepository {
    List<Customer> findAll();

    Customer findById(Long id);
}
