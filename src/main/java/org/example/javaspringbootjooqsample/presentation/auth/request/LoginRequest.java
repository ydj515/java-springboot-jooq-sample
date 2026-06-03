package org.example.javaspringbootjooqsample.presentation.auth.request;

import org.example.javaspringbootjooqsample.application.auth.command.LoginCommand;

public record LoginRequest(
        String username,
        String password
) {
    public LoginCommand toCommand() {
        return new LoginCommand(username, password);
    }
}
