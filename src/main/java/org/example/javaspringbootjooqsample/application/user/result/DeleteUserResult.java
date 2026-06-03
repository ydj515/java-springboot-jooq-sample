package org.example.javaspringbootjooqsample.application.user.result;

public record DeleteUserResult(
        Long id,
        int deletedCount
) {
}
