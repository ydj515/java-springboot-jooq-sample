package org.example.javaspringbootjooqsample.presentation.common;

import org.example.javaspringbootjooqsample.application.user.UserUseCase;
import org.example.javaspringbootjooqsample.application.order.OrderBatchUseCase;
import org.example.javaspringbootjooqsample.application.order.OrderUseCase;
import org.example.javaspringbootjooqsample.config.security.SecurityConfig;
import org.example.javaspringbootjooqsample.domain.user.exception.UserUsernameRequiredException;
import org.example.javaspringbootjooqsample.domain.user.exception.InvalidUserException;
import org.example.javaspringbootjooqsample.domain.order.OrderStatus;
import org.example.javaspringbootjooqsample.domain.order.exception.InvalidOrderStatusTransitionException;
import org.example.javaspringbootjooqsample.domain.order.exception.OrderItemsRequiredException;
import org.example.javaspringbootjooqsample.domain.order.exception.OrderNotFoundException;
import org.example.javaspringbootjooqsample.infrastructure.security.JwtTokenProvider;
import org.example.javaspringbootjooqsample.infrastructure.security.RestAccessDeniedHandler;
import org.example.javaspringbootjooqsample.infrastructure.security.RestAuthenticationEntryPoint;
import org.example.javaspringbootjooqsample.presentation.user.UserController;
import org.example.javaspringbootjooqsample.presentation.order.OrderController;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("web-layer")
@WebMvcTest({UserController.class, OrderController.class})
@Import({SecurityConfig.class, RestAuthenticationEntryPoint.class, RestAccessDeniedHandler.class})
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class GlobalExceptionHandlerWebMvcTests {

    private final MockMvc mockMvc;

    @MockitoBean
    private UserUseCase userUseCase;

    @MockitoBean
    private OrderUseCase orderUseCase;

    @MockitoBean
    private OrderBatchUseCase orderBatchUseCase;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    GlobalExceptionHandlerWebMvcTests(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    void returnsBadRequestWhenUsernameMissing() throws Exception {
        // given
        given(userUseCase.findByUsername(any())).willThrow(new UserUsernameRequiredException());

        // when
        mockMvc.perform(
                        get("/api/users/detail")
                                .with(SecurityMockMvcRequestPostProcessors.user("user123"))
                                .header("X-Trace-Id", "trace-error-username")
                )
                // then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("USER_USERNAME_REQUIRED"))
                .andExpect(jsonPath("$.message").value("계정 조회용 username은 필수입니다."))
                .andExpect(jsonPath("$.path").value("/api/users/detail"))
                .andExpect(jsonPath("$.traceId").value("trace-error-username"))
                .andExpect(header().string("X-Trace-Id", "trace-error-username"));
    }

    @Test
    void returnsBadRequestWhenCreateUserRequestInvalid() throws Exception {
        // given
        given(userUseCase.create(any())).willThrow(new InvalidUserException("계정 생성 시 username은 필수입니다."));

        // when
        mockMvc.perform(
                        post("/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "name": "No Username",
                                          "email": "nouser@example.com",
                                          "userType": "USER"
                                        }
                                        """)
                )
                // then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("USER_INVALID"))
                .andExpect(jsonPath("$.path").value("/api/users"));
    }

    @Test
    void returnsBadRequestWhenBatchItemsEmpty() throws Exception {
        // given
        given(orderBatchUseCase.addOrderItemsWithBatchSession(any())).willThrow(new OrderItemsRequiredException());

        // when
        mockMvc.perform(
                        post("/api/orders/1/items/batch-session")
                                .with(SecurityMockMvcRequestPostProcessors.user("user123"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "items": []
                                        }
                                        """)
                )
                // then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("ORDER_ITEMS_REQUIRED"))
                .andExpect(jsonPath("$.path").value("/api/orders/1/items/batch-session"));
    }

    @Test
    void returnsBadRequestWhenEnumQueryParameterInvalid() throws Exception {
        // given
        // enum 바인딩은 컨트롤러 진입 전에 실패하므로 별도 mock 설정이 필요 없습니다.

        // when
        mockMvc.perform(
                        get("/api/orders")
                                .with(SecurityMockMvcRequestPostProcessors.user("user123"))
                                .param("status", "UNKNOWN")
                )
                // then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST_PARAMETER"))
                .andExpect(jsonPath("$.path").value("/api/orders"));
    }

    @Test
    void returnsNotFoundWhenOrderDoesNotExist() throws Exception {
        // given
        given(orderUseCase.getOrder(any())).willThrow(new OrderNotFoundException(999L));

        // when
        mockMvc.perform(get("/api/orders/999").with(SecurityMockMvcRequestPostProcessors.user("user123")))
                // then
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ORDER_NOT_FOUND"))
                .andExpect(jsonPath("$.path").value("/api/orders/999"));
    }

    @Test
    void returnsConflictWhenOrderStatusTransitionInvalid() throws Exception {
        // given
        given(orderUseCase.cancelOrder(any())).willThrow(
                new InvalidOrderStatusTransitionException(
                        3L,
                        OrderStatus.SHIPPED,
                        OrderStatus.CANCELLED,
                        java.util.List.of(OrderStatus.CREATED, OrderStatus.PAID)
                )
        );

        // when
        mockMvc.perform(post("/api/orders/3/cancel")
                        .with(SecurityMockMvcRequestPostProcessors.user("user123"))
                        .header("Idempotency-Key", "test-cancel-key-handler"))
                // then
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INVALID_ORDER_STATUS_TRANSITION"))
                .andExpect(jsonPath("$.path").value("/api/orders/3/cancel"));
    }
}
