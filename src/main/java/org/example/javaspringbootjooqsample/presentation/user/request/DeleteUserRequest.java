package org.example.javaspringbootjooqsample.presentation.user.request;

import org.example.javaspringbootjooqsample.application.user.command.DeleteUserCommand;

public record DeleteUserRequest(Long id) {
    public static DeleteUserRequest from(Long id) {
        return new DeleteUserRequest(id);
    }

    public DeleteUserCommand toCommand() {
        return new DeleteUserCommand(id);
    }
}
