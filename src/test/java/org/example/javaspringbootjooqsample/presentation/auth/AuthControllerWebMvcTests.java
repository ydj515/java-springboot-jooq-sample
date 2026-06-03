package org.example.javaspringbootjooqsample.presentation.auth;

import org.example.javaspringbootjooqsample.application.auth.AuthUseCase;
import org.example.javaspringbootjooqsample.application.auth.result.LoginResult;
import org.example.javaspringbootjooqsample.config.security.SecurityConfig;
import org.example.javaspringbootjooqsample.infrastructure.security.JwtTokenProvider;
import org.example.javaspringbootjooqsample.infrastructure.security.RestAccessDeniedHandler;
import org.example.javaspringbootjooqsample.infrastructure.security.RestAuthenticationEntryPoint;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("web-layer")
@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, RestAuthenticationEntryPoint.class, RestAccessDeniedHandler.class})
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class AuthControllerWebMvcTests {

    private final MockMvc mockMvc;

    @MockitoBean
    private AuthUseCase authUseCase;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    AuthControllerWebMvcTests(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    void loginReturnsAccessTokenAndAuthorizationHeader() throws Exception {
        // given
        given(authUseCase.login(any())).willReturn(
                new LoginResult("user123", "Bearer", "access-token", 1_700_000_000_000L)
        );

        // when
        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "username": "user123",
                                          "password": "secret123"
                                        }
                                        """)
                )
                // then
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.AUTHORIZATION, "Bearer access-token"))
                .andExpect(jsonPath("$.username").value("user123"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.accessTokenExpiresAt").value(1_700_000_000_000L));
    }
}
