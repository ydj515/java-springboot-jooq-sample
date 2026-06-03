package org.example.javaspringbootjooqsample.application.auth.command;

public record LoginCommand(
        String username,
        String password
) {
}
