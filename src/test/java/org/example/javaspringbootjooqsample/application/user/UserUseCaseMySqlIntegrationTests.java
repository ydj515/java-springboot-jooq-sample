package org.example.javaspringbootjooqsample.application.user;

import org.example.javaspringbootjooqsample.application.user.command.CreateUserCommand;
import org.example.javaspringbootjooqsample.application.user.command.DeleteUserCommand;
import org.example.javaspringbootjooqsample.application.user.command.GetUserCommand;
import org.example.javaspringbootjooqsample.application.user.command.UpdateUserCommand;
import org.example.javaspringbootjooqsample.application.user.result.UserResult;
import org.example.javaspringbootjooqsample.domain.user.User;
import org.example.javaspringbootjooqsample.domain.user.UserType;
import org.example.javaspringbootjooqsample.domain.user.exception.UserNotFoundException;
import org.example.javaspringbootjooqsample.domain.user.repository.UserRepository;
import org.example.javaspringbootjooqsample.support.MySqlIntegrationTestSupport;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestConstructor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Tag("integration-test")
@SpringBootTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class UserUseCaseMySqlIntegrationTests extends MySqlIntegrationTestSupport {

    private final UserUseCase userUseCase;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    UserUseCaseMySqlIntegrationTests(
            UserUseCase userUseCase,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userUseCase = userUseCase;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Test
    void updateChangesNameEmailUserTypeAndTrialCount() {
        // given
        UserResult created = userUseCase.create(new CreateUserCommand(
                "update-target",
                "init-pass",
                "Initial Name",
                "initial@example.com",
                UserType.USER,
                0
        ));
        Long createdId = created.id();

        // when
        var updateResult = userUseCase.update(new UpdateUserCommand(
                createdId,
                "update-target",
                "init-pass",
                "Updated Name",
                "updated@example.com",
                null,
                null,
                null,
                null,
                UserType.MANAGER,
                3
        ));

        // then
        assertThat(updateResult.id()).isEqualTo(createdId);
        assertThat(updateResult.updatedCount()).isEqualTo(1);

        UserResult fetched = userUseCase.findById(new GetUserCommand(createdId));
        assertThat(fetched.name()).isEqualTo("Updated Name");
        assertThat(fetched.email()).isEqualTo("updated@example.com");
        assertThat(fetched.userType()).isEqualTo("MANAGER");
        assertThat(fetched.trialCount()).isEqualTo(3);
    }

    @Test
    void updateEncodesNewPasswordBeforePersisting() {
        // given
        UserResult created = userUseCase.create(new CreateUserCommand(
                "password-target",
                "first-pass",
                "P",
                "p@example.com",
                UserType.USER,
                0
        ));
        Long createdId = created.id();
        User beforeUpdate = userRepository.findById(createdId);
        String initialPassword = beforeUpdate.getPassword();

        // when
        userUseCase.update(new UpdateUserCommand(
                createdId,
                "password-target",
                "second-pass",
                "P3",
                "p3@example.com",
                null,
                null,
                null,
                null,
                UserType.USER,
                0
        ));

        // then
        User afterUpdate = userRepository.findById(createdId);
        assertThat(afterUpdate.getPassword()).isNotEqualTo(initialPassword);
        assertThat(passwordEncoder.matches("second-pass", afterUpdate.getPassword())).isTrue();
    }

    @Test
    void deleteRemovesExistingUserAndReturnsCountOne() {
        // given
        UserResult created = userUseCase.create(new CreateUserCommand(
                "delete-target",
                "pass1234",
                "Del",
                "del@example.com",
                UserType.USER,
                0
        ));
        Long createdId = created.id();

        // when
        var deleteResult = userUseCase.delete(new DeleteUserCommand(createdId));

        // then
        assertThat(deleteResult.id()).isEqualTo(createdId);
        assertThat(deleteResult.deletedCount()).isEqualTo(1);
        assertThat(userRepository.findById(createdId)).isNull();
    }

    @Test
    void deleteThrowsUserNotFoundExceptionForMissingId() {
        // given
        long missingId = 999_999_999L;

        // when & then
        assertThatThrownBy(() -> userUseCase.delete(new DeleteUserCommand(missingId)))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void findByIdAfterDeleteThrowsUserNotFoundException() {
        // given
        UserResult created = userUseCase.create(new CreateUserCommand(
                "delete-then-find",
                "pass1234",
                "X",
                "x@example.com",
                UserType.USER,
                0
        ));
        Long createdId = created.id();
        userUseCase.delete(new DeleteUserCommand(createdId));

        // when & then
        assertThatThrownBy(() -> userUseCase.findById(new GetUserCommand(createdId)))
                .isInstanceOf(UserNotFoundException.class);
    }
}
