package org.example.javaspringbootjooqsample.domain.customer.exception;

import org.example.javaspringbootjooqsample.common.error.BusinessException;
import org.springframework.http.HttpStatus;

public class CustomerNotFoundException extends BusinessException {

    public CustomerNotFoundException(Long id) {
        super(HttpStatus.NOT_FOUND, "CUSTOMER_NOT_FOUND", "고객을 찾을 수 없습니다. id=" + id);
    }
}
