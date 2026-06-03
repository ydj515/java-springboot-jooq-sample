package org.example.javaspringbootjooqsample.presentation.user.response;

import org.example.javaspringbootjooqsample.application.user.result.UpdateUserResult;

public record UpdateUserResponse(
        Long id,
        int updatedCount
) {
    public static UpdateUserResponse from(UpdateUserResult result) {
        return new UpdateUserResponse(
                result.id(),
                result.updatedCount()
        );
    }
}
