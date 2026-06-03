package org.example.javaspringbootjooqsample.domain.user.policy;

import org.example.javaspringbootjooqsample.domain.user.User;
import org.example.javaspringbootjooqsample.domain.user.UserType;
import org.example.javaspringbootjooqsample.domain.user.exception.InvalidUserException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Tag("unit")
class UserRegistrationPolicyUnitTests {

    private final UserRegistrationPolicy userRegistrationPolicy = new UserRegistrationPolicy();

    @Test
    void throwsWhenUsernameMissing() {
        // given
        User user = User.register(" ", "secret123", "홍길동", "hong@example.com", UserType.USER, 0);

        // when & then
        assertThatThrownBy(() -> userRegistrationPolicy.validate(user))
                .isInstanceOf(InvalidUserException.class)
                .hasMessage("계정 생성 시 username은 필수입니다.");
    }

    @Test
    void passesWhenRequiredFieldsProvided() {
        // given
        User user = User.register("user123", "secret123", "홍길동", "hong@example.com", UserType.USER, 0);

        // when & then
        assertThatCode(() -> userRegistrationPolicy.validate(user))
                .doesNotThrowAnyException();
    }
}
