package org.example.javaspringbootjooqsample.application.auth;

import lombok.RequiredArgsConstructor;
import org.example.javaspringbootjooqsample.application.auth.command.LoginCommand;
import org.example.javaspringbootjooqsample.application.auth.result.LoginResult;
import org.example.javaspringbootjooqsample.infrastructure.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthUseCase {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    public LoginResult login(LoginCommand command) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(command.username(), command.password())
        );
        JwtTokenProvider.IssuedAccessToken issuedAccessToken = jwtTokenProvider.issueAccessToken(authentication);

        return new LoginResult(
                authentication.getName(),
                issuedAccessToken.tokenType(),
                issuedAccessToken.accessToken(),
                issuedAccessToken.accessTokenExpiresAt()
        );
    }
}
