package org.example.javaspringbootjooqsample.infrastructure.security;

public final class AuthConstants {
    public static final String AUTH_CLAIM = "auth";
    public static final String BEARER = "Bearer";
    public static final String BEARER_SEPARATOR = " ";
    public static final String AUTHORIZATION_PREFIX = BEARER + BEARER_SEPARATOR;

    private AuthConstants() {
    }
}
