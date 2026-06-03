package org.example.javaspringbootjooqsample.application.order;

import org.example.javaspringbootjooqsample.application.compensation.CompensationOutcome;
import org.example.javaspringbootjooqsample.application.compensation.CompensationService;
import org.example.javaspringbootjooqsample.application.order.command.PayOrderCommand;
import org.example.javaspringbootjooqsample.application.order.result.OrderStatusChangeResult;
import org.example.javaspringbootjooqsample.domain.order.OrderStatus;
import org.example.javaspringbootjooqsample.domain.order.exception.OrderStateConflictException;
import org.example.javaspringbootjooqsample.domain.order.policy.OrderStatusTransitionPolicy;
import org.example.javaspringbootjooqsample.domain.order.repository.OrderRepository;
import org.example.javaspringbootjooqsample.domain.order.service.OrderItemAppendService;
import org.example.javaspringbootjooqsample.domain.order.service.OrderLookupService;
import org.example.javaspringbootjooqsample.domain.payment.exception.PaymentApprovalFailedException;
import org.example.javaspringbootjooqsample.domain.payment.gateway.ApproveResult;
import org.example.javaspringbootjooqsample.domain.payment.gateway.PaymentGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@Tag("unit")
class OrderUseCaseStatusTransitionUnitTests {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderLookupService orderLookupService;

    @Mock
    private OrderItemAppendService orderItemAppendService;

    @Mock
    private PaymentGateway paymentGateway;

    @Mock
    private CompensationService compensationService;

    @Mock
    private OrderPaymentTransactionService orderPaymentTransactionService;

    private OrderUseCase orderUseCase;

    @BeforeEach
    void setUp() {
        orderUseCase = new OrderUseCase(
                orderRepository,
                orderLookupService,
                orderItemAppendService,
                new OrderStatusTransitionPolicy(),
                null,
                null,
                paymentGateway,
                compensationService,
                orderPaymentTransactionService
        );
        given(compensationService.compensateApprovedPayment(any(), any(), any(), any(), any()))
                .willReturn(new CompensationOutcome.Refunded(LocalDateTime.of(2024, 7, 1, 9, 40)));
    }

    @Test
    void payOrderReturnsReplayResultWithoutCallingGateway() {
        OrderStatusChangeResult replayResult = new OrderStatusChangeResult(
                1L,
                "ORD-2024-0001",
                "PAID",
                1L,
                LocalDateTime.of(2024, 7, 1, 9, 35),
                null,
                null,
                "MOCK-PG-replay",
                LocalDateTime.of(2024, 7, 1, 9, 30),
                LocalDateTime.of(2024, 7, 1, 9, 35)
        );
        given(orderPaymentTransactionService.preparePayOrder(new PayOrderCommand(1L, "test-idempotency-key-1")))
                .willReturn(new PayOrderPreparation.Replay(replayResult));

        OrderStatusChangeResult actual = orderUseCase.payOrder(new PayOrderCommand(1L, "test-idempotency-key-1"));

        assertThat(actual).isEqualTo(replayResult);
        verify(paymentGateway, never()).approve(any(), any());
    }

    @Test
    void payOrderMarksPaymentFailedWhenGatewayDeclines() {
        PayOrderPreparation.ApprovalRequired preparation = approvalRequired("test-idempotency-key-1");
        given(orderPaymentTransactionService.preparePayOrder(new PayOrderCommand(1L, "test-idempotency-key-1")))
                .willReturn(preparation);
        given(paymentGateway.approve(any(BigDecimal.class), eq("test-idempotency-key-1")))
                .willThrow(new PaymentApprovalFailedException("PG declined: insufficient balance"));

        assertThatThrownBy(() -> orderUseCase.payOrder(new PayOrderCommand(1L, "test-idempotency-key-1")))
                .isInstanceOf(PaymentApprovalFailedException.class)
                .hasMessage("PG declined: insufficient balance");

        verify(orderPaymentTransactionService, times(1)).markPaymentFailed(
                eq(99L),
                eq("PG declined: insufficient balance"),
                any(LocalDateTime.class)
        );
        verify(orderPaymentTransactionService, never()).markPaymentApproved(any(), any(), any());
        verify(orderPaymentTransactionService, never()).completePayOrder(any(), any(), any(), any());
        verify(compensationService, never()).compensateApprovedPayment(any(), any(), any(), any(), any());
    }

    @Test
    void payOrderPropagatesUnexpectedGatewayErrorWithoutTouchingPaymentAudit() {
        PayOrderPreparation.ApprovalRequired preparation = approvalRequired("test-idempotency-key-1");
        given(orderPaymentTransactionService.preparePayOrder(new PayOrderCommand(1L, "test-idempotency-key-1")))
                .willReturn(preparation);
        given(paymentGateway.approve(any(BigDecimal.class), eq("test-idempotency-key-1")))
                .willThrow(new RuntimeException("network timeout"));

        assertThatThrownBy(() -> orderUseCase.payOrder(new PayOrderCommand(1L, "test-idempotency-key-1")))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("network timeout");

        verify(orderPaymentTransactionService, never()).markPaymentFailed(any(), any(), any());
        verify(orderPaymentTransactionService, never()).markPaymentApproved(any(), any(), any());
        verify(orderPaymentTransactionService, never()).completePayOrder(any(), any(), any(), any());
        verify(compensationService, never()).compensateApprovedPayment(any(), any(), any(), any(), any());
    }

    @Test
    void payOrderCompensatesWhenApprovalAuditPersistenceFails() {
        PayOrderPreparation.ApprovalRequired preparation = approvalRequired("test-idempotency-key-approval-fail");
        LocalDateTime approvedAt = LocalDateTime.of(2024, 7, 1, 9, 35);
        given(orderPaymentTransactionService.preparePayOrder(new PayOrderCommand(1L, "test-idempotency-key-approval-fail")))
                .willReturn(preparation);
        given(paymentGateway.approve(any(BigDecimal.class), eq("test-idempotency-key-approval-fail")))
                .willReturn(new ApproveResult("MOCK-PG-approval-fail", approvedAt));
        willThrow(new RuntimeException("payment audit write failed"))
                .given(orderPaymentTransactionService)
                .markPaymentApproved(99L, "MOCK-PG-approval-fail", approvedAt);

        assertThatThrownBy(() -> orderUseCase.payOrder(new PayOrderCommand(1L, "test-idempotency-key-approval-fail")))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("payment audit write failed");

        verify(compensationService, times(1)).compensateApprovedPayment(
                eq(99L),
                eq("MOCK-PG-approval-fail"),
                eq(new BigDecimal("169000.00")),
                any(String.class),
                any(LocalDateTime.class)
        );
        verify(orderPaymentTransactionService, never()).completePayOrder(any(), any(), any(), any());
    }

    @Test
    void payOrderCompensatesWhenCompletePayOrderFails() {
        PayOrderPreparation.ApprovalRequired preparation = approvalRequired("test-idempotency-key-scenario-b");
        LocalDateTime approvedAt = LocalDateTime.of(2024, 7, 1, 9, 35);
        given(orderPaymentTransactionService.preparePayOrder(new PayOrderCommand(1L, "test-idempotency-key-scenario-b")))
                .willReturn(preparation);
        given(paymentGateway.approve(any(BigDecimal.class), eq("test-idempotency-key-scenario-b")))
                .willReturn(new ApproveResult("MOCK-PG-scenario-b", approvedAt));
        given(orderPaymentTransactionService.completePayOrder(1L, 99L, "MOCK-PG-scenario-b", approvedAt))
                .willThrow(new OrderStateConflictException(1L, OrderStatus.CREATED, OrderStatus.PAID, 0L));

        assertThatThrownBy(() -> orderUseCase.payOrder(new PayOrderCommand(1L, "test-idempotency-key-scenario-b")))
                .isInstanceOf(OrderStateConflictException.class)
                .hasMessageContaining("충돌");

        verify(compensationService, times(1)).compensateApprovedPayment(
                eq(99L),
                eq("MOCK-PG-scenario-b"),
                eq(new BigDecimal("169000.00")),
                any(String.class),
                any(LocalDateTime.class)
        );
    }

    private PayOrderPreparation.ApprovalRequired approvalRequired(String idempotencyKey) {
        return new PayOrderPreparation.ApprovalRequired(
                1L,
                99L,
                new BigDecimal("169000.00"),
                idempotencyKey
        );
    }
}
