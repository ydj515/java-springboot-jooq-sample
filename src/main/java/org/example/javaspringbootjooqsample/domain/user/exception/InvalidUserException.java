package org.example.javaspringbootjooqsample.domain.user.exception;

import org.example.javaspringbootjooqsample.common.error.BusinessException;
import org.springframework.http.HttpStatus;

public class InvalidUserException extends BusinessException {
    public InvalidUserException(String message) {
        super(HttpStatus.BAD_REQUEST, "USER_INVALID", message);
    }
}
