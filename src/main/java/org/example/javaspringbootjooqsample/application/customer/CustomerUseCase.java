package org.example.javaspringbootjooqsample.application.customer;

import lombok.RequiredArgsConstructor;
import org.example.javaspringbootjooqsample.application.customer.command.FindCustomersCommand;
import org.example.javaspringbootjooqsample.application.customer.command.GetCustomerCommand;
import org.example.javaspringbootjooqsample.application.customer.result.CustomerResult;
import org.example.javaspringbootjooqsample.domain.customer.repository.CustomerRepository;
import org.example.javaspringbootjooqsample.domain.customer.service.CustomerLookupService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerUseCase {

    private final CustomerRepository customerRepository;
    private final CustomerLookupService customerLookupService;

    public List<CustomerResult> findAll(FindCustomersCommand command) {
        return customerRepository.findAll().stream()
                .map(CustomerResult::from)
                .toList();
    }

    public CustomerResult findById(GetCustomerCommand command) {
        Long id = command == null ? null : command.id();
        return CustomerResult.from(customerLookupService.requireById(id));
    }
}
