package org.example.javaspringbootjooqsample.application.user.command;

import org.example.javaspringbootjooqsample.domain.user.UserType;

import java.time.LocalDateTime;

public record UpdateUserCommand(
        Long id,
        String username,
        String password,
        String name,
        String email,
        LocalDateTime lastLoginAt,
        LocalDateTime updatedAt,
        LocalDateTime deletedAt,
        LocalDateTime lastPasswordUpdatedAt,
        UserType userType,
        int trialCount
) {
}
