package org.example.javaspringbootjooqsample.domain.user.exception;

import org.example.javaspringbootjooqsample.common.error.BusinessException;
import org.springframework.http.HttpStatus;

public class UserAlreadyExistsException extends BusinessException {
    public UserAlreadyExistsException(String username) {
        super(HttpStatus.CONFLICT, "USER_ALREADY_EXISTS", "이미 존재하는 계정입니다. username=" + username);
    }
}
