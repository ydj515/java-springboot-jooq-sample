package org.example.javaspringbootjooqsample.presentation.user.response;

import org.example.javaspringbootjooqsample.application.user.result.UserRoleResult;

import java.time.LocalDateTime;

public record UserRoleResponse(
        Long id,
        String name,
        String description,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static UserRoleResponse from(UserRoleResult result) {
        return new UserRoleResponse(
                result.id(),
                result.name(),
                result.description(),
                result.createdAt(),
                result.updatedAt()
        );
    }
}
