package org.example.javaspringbootjooqsample.domain.user.exception;

import org.example.javaspringbootjooqsample.common.error.BusinessException;
import org.springframework.http.HttpStatus;

public class UserUsernameRequiredException extends BusinessException {
    public UserUsernameRequiredException() {
        super(HttpStatus.BAD_REQUEST, "USER_USERNAME_REQUIRED", "계정 조회용 username은 필수입니다.");
    }
}
