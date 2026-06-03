package org.example.javaspringbootjooqsample.presentation.customer;

import lombok.RequiredArgsConstructor;
import org.example.javaspringbootjooqsample.application.customer.CustomerUseCase;
import org.example.javaspringbootjooqsample.application.customer.command.FindCustomersCommand;
import org.example.javaspringbootjooqsample.application.customer.command.GetCustomerCommand;
import org.example.javaspringbootjooqsample.application.customer.result.CustomerResult;
import org.example.javaspringbootjooqsample.presentation.customer.response.CustomerResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerUseCase customerUseCase;

    @GetMapping("")
    public ResponseEntity<List<CustomerResponse>> getCustomers() {
        List<CustomerResponse> customers = customerUseCase.findAll(FindCustomersCommand.empty()).stream()
                .map(CustomerResponse::from)
                .toList();
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> getCustomer(@PathVariable Long id) {
        CustomerResult result = customerUseCase.findById(new GetCustomerCommand(id));
        return ResponseEntity.ok(CustomerResponse.from(result));
    }
}
