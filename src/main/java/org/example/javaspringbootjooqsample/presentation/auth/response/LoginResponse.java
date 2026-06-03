package org.example.javaspringbootjooqsample.presentation.auth.response;

import org.example.javaspringbootjooqsample.application.auth.result.LoginResult;

public record LoginResponse(
        String username,
        String tokenType,
        String accessToken,
        long accessTokenExpiresAt
) {
    public static LoginResponse from(LoginResult result) {
        return new LoginResponse(
                result.username(),
                result.tokenType(),
                result.accessToken(),
                result.accessTokenExpiresAt()
        );
    }
}
