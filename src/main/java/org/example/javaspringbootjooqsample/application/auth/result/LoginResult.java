package org.example.javaspringbootjooqsample.application.auth.result;

public record LoginResult(
        String username,
        String tokenType,
        String accessToken,
        long accessTokenExpiresAt
) {
}
