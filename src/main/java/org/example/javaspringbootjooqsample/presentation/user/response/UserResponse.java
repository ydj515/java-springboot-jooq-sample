package org.example.javaspringbootjooqsample.presentation.user.response;

import org.example.javaspringbootjooqsample.application.user.result.UserResult;

import java.time.LocalDateTime;
import java.util.List;

public record UserResponse(
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
        List<UserRoleResponse> roles
) {
    public static UserResponse from(UserResult result) {
        return new UserResponse(
                result.id(),
                result.username(),
                result.name(),
                result.email(),
                result.userType(),
                result.lastLoginAt(),
                result.createdAt(),
                result.updatedAt(),
                result.deletedAt(),
                result.lastPasswordUpdatedAt(),
                result.trialCount(),
                result.roles().stream()
                        .map(UserRoleResponse::from)
                        .toList()
        );
    }
}
