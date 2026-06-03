package org.example.javaspringbootjooqsample.infrastructure.customer;

import lombok.RequiredArgsConstructor;
import org.example.javaspringbootjooqsample.domain.customer.Customer;
import org.example.javaspringbootjooqsample.domain.customer.repository.CustomerRepository;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.springframework.stereotype.Repository;

import java.util.List;

import static org.example.javaspringbootjooqsample.generated.jooq_codegen.tables.Customers.CUSTOMERS;

@Repository
@RequiredArgsConstructor
public class CustomerJooqRepositoryAdapter implements CustomerRepository {

    private final DSLContext dsl;

    @Override
    public List<Customer> findAll() {
        return dsl.select(
                        CUSTOMERS.ID,
                        CUSTOMERS.NAME,
                        CUSTOMERS.EMAIL,
                        CUSTOMERS.CREATED_AT,
                        CUSTOMERS.UPDATED_AT
                )
                .from(CUSTOMERS)
                .orderBy(CUSTOMERS.ID.asc())
                .fetch(this::toCustomer);
    }

    @Override
    public Customer findById(Long id) {
        return dsl.select(
                        CUSTOMERS.ID,
                        CUSTOMERS.NAME,
                        CUSTOMERS.EMAIL,
                        CUSTOMERS.CREATED_AT,
                        CUSTOMERS.UPDATED_AT
                )
                .from(CUSTOMERS)
                .where(CUSTOMERS.ID.eq(id))
                .fetchOne(this::toCustomer);
    }

    private Customer toCustomer(Record record) {
        return Customer.builder()
                .id(record.get(CUSTOMERS.ID))
                .name(record.get(CUSTOMERS.NAME))
                .email(record.get(CUSTOMERS.EMAIL))
                .createdAt(record.get(CUSTOMERS.CREATED_AT))
                .updatedAt(record.get(CUSTOMERS.UPDATED_AT))
                .build();
    }
}
