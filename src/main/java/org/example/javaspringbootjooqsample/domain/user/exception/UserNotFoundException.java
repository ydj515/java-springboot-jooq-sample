package org.example.javaspringbootjooqsample.domain.user.exception;

import org.example.javaspringbootjooqsample.common.error.BusinessException;
import org.springframework.http.HttpStatus;

public class UserNotFoundException extends BusinessException {

    public UserNotFoundException(Long id) {
        super(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "계정을 찾을 수 없습니다. id=" + id);
    }

    private UserNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", message);
    }

    public static UserNotFoundException byUsername(String username) {
        return new UserNotFoundException("계정을 찾을 수 없습니다. username=" + username);
    }
}
