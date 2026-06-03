package org.example.javaspringbootjooqsample.presentation.user;

import org.example.javaspringbootjooqsample.application.user.UserUseCase;
import org.example.javaspringbootjooqsample.application.user.command.GetUserCommand;
import org.example.javaspringbootjooqsample.application.user.result.UserResult;
import org.example.javaspringbootjooqsample.application.user.result.DeleteUserResult;
import org.example.javaspringbootjooqsample.config.security.SecurityConfig;
import org.example.javaspringbootjooqsample.domain.user.exception.UserAlreadyExistsException;
import org.example.javaspringbootjooqsample.domain.user.exception.UserNotFoundException;
import org.example.javaspringbootjooqsample.infrastructure.security.JwtTokenProvider;
import org.example.javaspringbootjooqsample.infrastructure.security.RestAccessDeniedHandler;
import org.example.javaspringbootjooqsample.infrastructure.security.RestAuthenticationEntryPoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("web-layer")
@WebMvcTest(UserController.class)
@Import({SecurityConfig.class, RestAuthenticationEntryPoint.class, RestAccessDeniedHandler.class})
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class UserControllerWebMvcTests {

    private final MockMvc mockMvc;

    @MockitoBean
    private UserUseCase userUseCase;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    UserControllerWebMvcTests(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    void createUserReturnsCreatedUserResponse() throws Exception {
        // given
        given(userUseCase.create(any())).willReturn(userResult(3L, "new-user", "New User", "USER"));

        // when
        mockMvc.perform(
                        post("/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "username": "new-user",
                                          "password": "secret123",
                                          "name": "New User",
                                          "email": "new.user@example.com",
                                          "userType": "USER",
                                          "trialCount": 0
                                        }
                                        """)
                )
                // then
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.username").value("new-user"))
                .andExpect(jsonPath("$.name").value("New User"))
                .andExpect(jsonPath("$.userType").value("USER"));
    }

    @Test
    void deleteUserRemovesUser() throws Exception {
        // given
        given(userUseCase.delete(any())).willReturn(new DeleteUserResult(2L, 1));
        given(userUseCase.findById(any(GetUserCommand.class))).willThrow(new UserNotFoundException(2L));

        // when
        mockMvc.perform(delete("/api/users/2").with(SecurityMockMvcRequestPostProcessors.user("user123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.deletedCount").value(1));

        // then
        mockMvc.perform(get("/api/users/2").with(SecurityMockMvcRequestPostProcessors.user("user123")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"));
    }

    @Test
    void createUserReturnsConflictWhenUsernameAlreadyExists() throws Exception {
        // given
        given(userUseCase.create(any())).willThrow(new UserAlreadyExistsException("user123"));

        // when
        mockMvc.perform(
                        post("/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "username": "user123",
                                          "password": "secret123",
                                          "name": "Duplicated User",
                                          "email": "dup@example.com",
                                          "userType": "USER",
                                          "trialCount": 0
                                        }
                                        """)
                )
                // then
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("USER_ALREADY_EXISTS"));
    }

    @Test
    void addsTraceHeadersToResponse() throws Exception {
        // given
        given(userUseCase.findById(any(GetUserCommand.class)))
                .willReturn(userResult(1L, "user123", "John Doe", "USER"));

        // when
        mockMvc.perform(
                        get("/api/users/1")
                                .with(SecurityMockMvcRequestPostProcessors.user("user123"))
                                .header("X-Request-Id", "request-id-123")
                )
                // then
                .andExpect(status().isOk())
                .andExpect(header().string("X-Request-Id", "request-id-123"))
                .andExpect(header().exists("X-Trace-Id"));
    }

    @Test
    void getUserRequiresAuthentication() throws Exception {
        // given

        // when
        mockMvc.perform(get("/api/users/1"))
                // then
                .andExpect(status().isUnauthorized());
    }

    private UserResult userResult(Long id, String username, String name, String userType) {
        return new UserResult(
                id,
                username,
                name,
                username + "@example.com",
                userType,
                null,
                null,
                null,
                null,
                null,
                0,
                List.of()
        );
    }
}
