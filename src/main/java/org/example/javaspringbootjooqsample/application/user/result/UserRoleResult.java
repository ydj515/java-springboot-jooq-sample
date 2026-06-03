package org.example.javaspringbootjooqsample.application.user.result;

import org.example.javaspringbootjooqsample.domain.user.Role;

import java.time.LocalDateTime;

public record UserRoleResult(
        Long id,
        String name,
        String description,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static UserRoleResult from(Role role) {
        return new UserRoleResult(
                role.getId(),
                role.getName(),
                role.getDescription(),
                role.getCreatedAt(),
                role.getUpdatedAt()
        );
    }
}
