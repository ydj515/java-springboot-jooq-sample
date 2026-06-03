package org.example.javaspringbootjooqsample.presentation.auth;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.javaspringbootjooqsample.application.auth.AuthUseCase;
import org.example.javaspringbootjooqsample.application.auth.result.LoginResult;
import org.example.javaspringbootjooqsample.infrastructure.security.AuthConstants;
import org.example.javaspringbootjooqsample.presentation.auth.request.LoginRequest;
import org.example.javaspringbootjooqsample.presentation.auth.response.LoginResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthUseCase authUseCase;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @RequestBody LoginRequest request,
            HttpServletResponse response
    ) {
        LoginResult result = authUseCase.login(request.toCommand());
        response.setHeader(HttpHeaders.AUTHORIZATION, result.tokenType() + AuthConstants.BEARER_SEPARATOR + result.accessToken());
        return ResponseEntity.ok(LoginResponse.from(result));
    }
}
