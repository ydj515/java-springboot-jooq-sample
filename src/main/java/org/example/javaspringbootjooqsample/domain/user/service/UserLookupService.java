package org.example.javaspringbootjooqsample.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.example.javaspringbootjooqsample.domain.user.User;
import org.example.javaspringbootjooqsample.domain.user.exception.UserIdRequiredException;
import org.example.javaspringbootjooqsample.domain.user.exception.UserNotFoundException;
import org.example.javaspringbootjooqsample.domain.user.exception.UserUsernameRequiredException;
import org.example.javaspringbootjooqsample.domain.user.repository.UserRepository;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class UserLookupService {

    private final UserRepository userRepository;

    public User requireById(Long id) {
        if (id == null) {
            throw new UserIdRequiredException();
        }

        User user = userRepository.findById(id);
        if (user == null) {
            throw new UserNotFoundException(id);
        }

        return user;
    }

    public User requireByUsername(String username) {
        if (!StringUtils.hasText(username)) {
            throw new UserUsernameRequiredException();
        }

        String normalizedUsername = username.trim();
        User user = userRepository.findByUsername(normalizedUsername);
        if (user == null) {
            throw UserNotFoundException.byUsername(normalizedUsername);
        }

        return user;
    }
}
