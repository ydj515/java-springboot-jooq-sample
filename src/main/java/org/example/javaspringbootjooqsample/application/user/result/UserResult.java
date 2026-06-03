package org.example.javaspringbootjooqsample.application.user.result;

import org.example.javaspringbootjooqsample.domain.user.User;
import org.example.javaspringbootjooqsample.domain.user.Role;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public record UserResult(
        Long id,
        String username,
        String name,
        String email,
        String userType,
        LocalDateTime lastLoginAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime deletedAt,
        LocalDateTime lastPasswordUpdatedAt,
        int trialCount,
        List<UserRoleResult> roles
) {
    public static UserResult from(User user) {
        return new UserResult(
                user.getId(),
                user.getUsername(),
                user.getName(),
                user.getEmail(),
                user.getUserType() == null ? null : user.getUserType().name(),
                user.getLastLoginAt(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getDeletedAt(),
                user.getLastPasswordUpdatedAt(),
                user.getTrialCount(),
                mapRoles(user.getRoles())
        );
    }

    private static List<UserRoleResult> mapRoles(Set<Role> roles) {
        if (roles == null || roles.isEmpty()) {
            return List.of();
        }

        return roles.stream()
                .map(UserRoleResult::from)
                .sorted(Comparator.comparing(UserRoleResult::id, Comparator.nullsLast(Long::compareTo)))
                .toList();
    }
}
