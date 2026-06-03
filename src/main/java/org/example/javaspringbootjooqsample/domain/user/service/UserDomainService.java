package org.example.javaspringbootjooqsample.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.example.javaspringbootjooqsample.domain.user.User;
import org.example.javaspringbootjooqsample.domain.user.exception.UserAlreadyExistsException;
import org.example.javaspringbootjooqsample.domain.user.exception.UserIdRequiredException;
import org.example.javaspringbootjooqsample.domain.user.exception.UserNotFoundException;
import org.example.javaspringbootjooqsample.domain.user.policy.UserRegistrationPolicy;
import org.example.javaspringbootjooqsample.domain.user.repository.UserRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserDomainService {

    private final UserRepository userRepository;
    private final UserLookupService userLookupService;
    private final UserRegistrationPolicy userRegistrationPolicy;

    public User register(User user) {
        userRegistrationPolicy.validate(user);
        user.normalizeRegistrationFields();

        if (userRepository.findByUsername(user.getUsername()) != null) {
            throw new UserAlreadyExistsException(user.getUsername());
        }

        userRepository.save(user);
        return userLookupService.requireById(user.getId());
    }

    public int update(User user) {
        if (user == null || user.getId() == null) {
            throw new UserIdRequiredException();
        }

        int updatedCount = userRepository.update(user);
        if (updatedCount == 0) {
            throw new UserNotFoundException(user.getId());
        }

        return updatedCount;
    }

    public int deleteById(Long id) {
        User user = userLookupService.requireById(id);

        userRepository.deleteUserRolesByUserId(user.getId());
        int deletedCount = userRepository.delete(user.getId());
        if (deletedCount == 0) {
            throw new UserNotFoundException(user.getId());
        }

        return deletedCount;
    }
}
