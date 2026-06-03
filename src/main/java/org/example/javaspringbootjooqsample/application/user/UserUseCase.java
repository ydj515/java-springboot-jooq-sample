package org.example.javaspringbootjooqsample.application.user;

import lombok.RequiredArgsConstructor;
import org.example.javaspringbootjooqsample.application.user.command.CreateUserCommand;
import org.example.javaspringbootjooqsample.application.user.command.DeleteUserCommand;
import org.example.javaspringbootjooqsample.application.user.command.FindUsersCommand;
import org.example.javaspringbootjooqsample.application.user.command.GetUserByUsernameCommand;
import org.example.javaspringbootjooqsample.application.user.command.GetUserCommand;
import org.example.javaspringbootjooqsample.application.user.command.UpdateUserCommand;
import org.example.javaspringbootjooqsample.application.user.result.UserResult;
import org.example.javaspringbootjooqsample.application.user.result.DeleteUserResult;
import org.example.javaspringbootjooqsample.application.user.result.UpdateUserResult;
import org.example.javaspringbootjooqsample.domain.user.User;
import org.example.javaspringbootjooqsample.domain.user.repository.UserRepository;
import org.example.javaspringbootjooqsample.domain.user.service.UserDomainService;
import org.example.javaspringbootjooqsample.domain.user.service.UserLookupService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserUseCase {
    private final UserRepository userRepository;
    private final UserLookupService userLookupService;
    private final UserDomainService userDomainService;
    private final PasswordEncoder passwordEncoder;

    public List<UserResult> findAll(FindUsersCommand command) {
        return userRepository.findAll().stream()
                .map(UserResult::from)
                .toList();
    }

    public UserResult findById(GetUserCommand command) {
        Long id = command == null ? null : command.id();
        return UserResult.from(userLookupService.requireById(id));
    }

    public UserResult findByUsername(GetUserByUsernameCommand command) {
        String username = command == null ? null : command.username();
        return UserResult.from(userLookupService.requireByUsername(username));
    }

    @Transactional
    public UserResult create(CreateUserCommand command) {
        User createdUser = userDomainService.register(
                User.register(
                        command.username(),
                        passwordEncoder.encode(command.password()),
                        command.name(),
                        command.email(),
                        command.userType(),
                        command.trialCount()
                )
        );

        return UserResult.from(createdUser);
    }

    @Transactional
    public UpdateUserResult update(UpdateUserCommand command) {
        User user = User.restoreForUpdate(
                command.id(),
                command.username(),
                encodePassword(command.password()),
                command.name(),
                command.email(),
                command.lastLoginAt(),
                command.updatedAt(),
                command.deletedAt(),
                command.lastPasswordUpdatedAt(),
                command.userType(),
                command.trialCount()
        );

        int updatedCount = userDomainService.update(user);
        return new UpdateUserResult(command.id(), updatedCount);
    }

    @Transactional
    public DeleteUserResult delete(DeleteUserCommand command) {
        Long id = command == null ? null : command.id();
        int deletedCount = userDomainService.deleteById(id);
        return new DeleteUserResult(id, deletedCount);
    }

    private String encodePassword(String rawPassword) {
        if (!StringUtils.hasText(rawPassword)) {
            return rawPassword;
        }

        return passwordEncoder.encode(rawPassword);
    }
}
