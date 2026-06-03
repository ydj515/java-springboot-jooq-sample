package org.example.javaspringbootjooqsample.application.user.command;

import org.example.javaspringbootjooqsample.domain.user.UserType;

public record CreateUserCommand(
        String username,
        String password,
        String name,
        String email,
        UserType userType,
        int trialCount
) {
}
