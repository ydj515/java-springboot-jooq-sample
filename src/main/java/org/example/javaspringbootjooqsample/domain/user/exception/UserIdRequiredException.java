package org.example.javaspringbootjooqsample.domain.user.exception;

import org.example.javaspringbootjooqsample.common.error.BusinessException;
import org.springframework.http.HttpStatus;

public class UserIdRequiredException extends BusinessException {
    public UserIdRequiredException() {
        super(HttpStatus.BAD_REQUEST, "USER_ID_REQUIRED", "계정 식별자(id)는 필수입니다.");
    }
}
