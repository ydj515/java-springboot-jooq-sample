package org.example.javaspringbootjooqsample.presentation.user.response;

import org.example.javaspringbootjooqsample.application.user.result.DeleteUserResult;

public record DeleteUserResponse(
        Long id,
        int deletedCount
) {
    public static DeleteUserResponse from(DeleteUserResult result) {
        return new DeleteUserResponse(
                result.id(),
                result.deletedCount()
        );
    }
}
