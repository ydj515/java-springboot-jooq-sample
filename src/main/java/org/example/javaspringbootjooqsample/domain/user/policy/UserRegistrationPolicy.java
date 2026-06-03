package org.example.javaspringbootjooqsample.domain.user.policy;

import org.example.javaspringbootjooqsample.domain.user.User;
import org.example.javaspringbootjooqsample.domain.user.exception.InvalidUserException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class UserRegistrationPolicy {

    public void validate(User user) {
        if (user == null) {
            throw new InvalidUserException("계정 생성 요청은 비어 있을 수 없습니다.");
        }

        if (!StringUtils.hasText(user.getUsername())) {
            throw new InvalidUserException("계정 생성 시 username은 필수입니다.");
        }

        if (!StringUtils.hasText(user.getPassword())) {
            throw new InvalidUserException("계정 생성 시 password는 필수입니다.");
        }

        if (!StringUtils.hasText(user.getName())) {
            throw new InvalidUserException("계정 생성 시 name은 필수입니다.");
        }

        if (!StringUtils.hasText(user.getEmail())) {
            throw new InvalidUserException("계정 생성 시 email은 필수입니다.");
        }

        if (user.getUserType() == null) {
            throw new InvalidUserException("계정 생성 시 userType은 필수입니다.");
        }
    }
}
