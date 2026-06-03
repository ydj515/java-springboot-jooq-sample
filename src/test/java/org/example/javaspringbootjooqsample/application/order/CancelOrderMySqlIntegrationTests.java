package org.example.javaspringbootjooqsample.application.order;

import org.example.javaspringbootjooqsample.application.order.command.CancelOrderCommand;
import org.example.javaspringbootjooqsample.application.order.command.PayOrderCommand;
import org.example.javaspringbootjooqsample.domain.compensation.CompensationTask;
import org.example.javaspringbootjooqsample.domain.compensation.CompensationTaskStatus;
import org.example.javaspringbootjooqsample.domain.compensation.repository.CompensationTaskRepository;
import org.example.javaspringbootjooqsample.domain.order.Cancellation;
import org.example.javaspringbootjooqsample.domain.order.CancellationStatus;
import org.example.javaspringbootjooqsample.domain.order.OrderStatus;
import org.example.javaspringbootjooqsample.domain.order.repository.CancellationRepository;
import org.example.javaspringbootjooqsample.domain.payment.Payment;
import org.example.javaspringbootjooqsample.domain.payment.PaymentStatus;
import org.example.javaspringbootjooqsample.domain.payment.exception.IdempotencyConflictException;
import org.example.javaspringbootjooqsample.domain.payment.gateway.ApproveResult;
import org.example.javaspringbootjooqsample.domain.payment.gateway.PaymentGateway;
import org.example.javaspringbootjooqsample.domain.payment.gateway.RefundResult;
import org.example.javaspringbootjooqsample.domain.payment.repository.PaymentRepository;
import org.example.javaspringbootjooqsample.support.MySqlIntegrationTestSupport;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.TestConstructor;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@Tag("integration-test")
@SpringBootTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class CancelOrderMySqlIntegrationTests extends MySqlIntegrationTestSupport {

    private final OrderUseCase orderUseCase;
    private final PaymentRepository paymentRepository;
    private final CancellationRepository cancellationRepository;
    private final CompensationTaskRepository compensationTaskRepository;
    private final JdbcTemplate jdbcTemplate;

    @MockitoBean
    private PaymentGateway paymentGateway;

    CancelOrderMySqlIntegrationTests(
            OrderUseCase orderUseCase,
            PaymentRepository paymentRepository,
            CancellationRepository cancellationRepository,
            CompensationTaskRepository compensationTaskRepository,
            JdbcTemplate jdbcTemplate
    ) {
        this.orderUseCase = orderUseCase;
        this.paymentRepository = paymentRepository;
        this.cancellationRepository = cancellationRepository;
        this.compensationTaskRepository = compensationTaskRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Test
    void scenarioC1_cancelPaidWithRefundSuccess() {
        long orderId = setupPaidOrder(new BigDecimal("10000.00"));
        Payment payment = paymentRepository.findByOrderId(orderId).get(0);
        String cancelKey = "cancel-c1-" + UUID.randomUUID();
        AtomicBoolean refundTxActive = new AtomicBoolean(true);

        given(paymentGateway.refund(eq(payment.getPaymentKey()), any()))
                .willAnswer(invocation -> {
                    refundTxActive.set(TransactionSynchronizationManager.isActualTransactionActive());
                    return new RefundResult(LocalDateTime.now().withNano(0));
                });

        orderUseCase.cancelOrder(new CancelOrderCommand(orderId, cancelKey, "고객 요청"));

        assertThat(refundTxActive).isFalse();
        assertThat(orderStatusOf(orderId)).isEqualTo(OrderStatus.CANCELLED);
        Payment refreshed = paymentRepository.findById(payment.getId());
        assertThat(refreshed.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
        Cancellation cancellation = cancellationRepository.findByIdempotencyKey(cancelKey);
        assertThat(cancellation.getStatus()).isEqualTo(CancellationStatus.SUCCEEDED);
        assertThat(cancellation.getRefundedAt()).isNotNull();
        assertThat(compensationTaskRepository.findPendingForUpdate(LocalDateTime.now().plusSeconds(1), 100)).isEmpty();
    }

    @Test
    void scenarioC2_cancelPaidWithRefundFailure() {
        long orderId = setupPaidOrder(new BigDecimal("12000.00"));
        Payment payment = paymentRepository.findByOrderId(orderId).get(0);
        String cancelKey = "cancel-c2-" + UUID.randomUUID();
        AtomicBoolean refundTxActive = new AtomicBoolean(true);

        given(paymentGateway.refund(eq(payment.getPaymentKey()), any()))
                .willAnswer(invocation -> {
                    refundTxActive.set(TransactionSynchronizationManager.isActualTransactionActive());
                    throw new RuntimeException("simulated PG refund failure");
                });

        orderUseCase.cancelOrder(new CancelOrderCommand(orderId, cancelKey, "테스트"));

        assertThat(refundTxActive).isFalse();
        assertThat(orderStatusOf(orderId)).isEqualTo(OrderStatus.CANCELLED);
        Payment refreshed = paymentRepository.findById(payment.getId());
        assertThat(refreshed.getStatus()).isEqualTo(PaymentStatus.REFUND_FAILED);
        Cancellation cancellation = cancellationRepository.findByIdempotencyKey(cancelKey);
        assertThat(cancellation.getStatus()).isEqualTo(CancellationStatus.REFUND_FAILED);

        List<CompensationTask> tasks = compensationTaskRepository.findPendingForUpdate(LocalDateTime.now().plusSeconds(1), 100);
        assertThat(tasks).hasSize(1);
        assertThat(tasks.get(0).getStatus()).isEqualTo(CompensationTaskStatus.PENDING);
    }

    @Test
    void scenarioD_cancelCreatedOrderNoRefundCall() {
        long orderId = setupCreatedOrder(new BigDecimal("5000.00"));
        String cancelKey = "cancel-d-" + UUID.randomUUID();

        orderUseCase.cancelOrder(new CancelOrderCommand(orderId, cancelKey, "변심"));

        assertThat(orderStatusOf(orderId)).isEqualTo(OrderStatus.CANCELLED);
        Cancellation cancellation = cancellationRepository.findByIdempotencyKey(cancelKey);
        assertThat(cancellation.getStatus()).isEqualTo(CancellationStatus.SUCCEEDED);
        assertThat(cancellation.getRefundedAt()).isNull();
        verify(paymentGateway, never()).refund(any(), any());
    }

    @Test
    void cancelIdempotencyReplayDoesNotInvokeRefundTwice() {
        long orderId = setupPaidOrder(new BigDecimal("11000.00"));
        Payment payment = paymentRepository.findByOrderId(orderId).get(0);
        String cancelKey = "cancel-replay-" + UUID.randomUUID();

        given(paymentGateway.refund(eq(payment.getPaymentKey()), any()))
                .willReturn(new RefundResult(LocalDateTime.now().withNano(0)));

        orderUseCase.cancelOrder(new CancelOrderCommand(orderId, cancelKey, "1회차"));
        orderUseCase.cancelOrder(new CancelOrderCommand(orderId, cancelKey, "1회차"));

        verify(paymentGateway, times(1)).refund(any(), any());
    }

    @Test
    void cancelIdempotencyConflictForDifferentOrder() {
        long firstOrderId = setupCreatedOrder(new BigDecimal("3000.00"));
        long secondOrderId = setupCreatedOrder(new BigDecimal("4000.00"));
        String cancelKey = "cancel-order-conflict-" + UUID.randomUUID();

        orderUseCase.cancelOrder(new CancelOrderCommand(firstOrderId, cancelKey, "first"));

        assertThatThrownBy(() ->
                orderUseCase.cancelOrder(new CancelOrderCommand(secondOrderId, cancelKey, "first"))
        ).isInstanceOf(IdempotencyConflictException.class);
    }

    @Test
    void cancelIdempotencyConflictForDifferentReason() {
        long orderId = setupCreatedOrder(new BigDecimal("3500.00"));
        String cancelKey = "cancel-reason-conflict-" + UUID.randomUUID();

        orderUseCase.cancelOrder(new CancelOrderCommand(orderId, cancelKey, "original"));

        assertThatThrownBy(() ->
                orderUseCase.cancelOrder(new CancelOrderCommand(orderId, cancelKey, "different"))
        ).isInstanceOf(IdempotencyConflictException.class);
    }

    private long setupCreatedOrder(BigDecimal amount) {
        String customerName = "통합-" + UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO customers (name, email) VALUES (?, ?)",
                customerName, "it-cancel@example.com"
        );
        Long customerId = jdbcTemplate.queryForObject(
                "SELECT id FROM customers WHERE name = ?", Long.class, customerName
        );
        String orderNo = "ORD-IT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        jdbcTemplate.update(
                "INSERT INTO purchase_orders (customer_id, order_no, order_status, version, total_amount, ordered_at, created_at, updated_at) " +
                        "VALUES (?, ?, 'CREATED', 0, ?, NOW(), NOW(), NOW())",
                customerId, orderNo, amount
        );
        Long orderId = jdbcTemplate.queryForObject(
                "SELECT id FROM purchase_orders WHERE order_no = ?", Long.class, orderNo
        );
        jdbcTemplate.update(
                "INSERT INTO purchase_order_items (order_id, product_name, quantity, unit_price, line_amount) " +
                        "VALUES (?, 'TestItem', 1, ?, ?)",
                orderId, amount, amount
        );
        return orderId;
    }

    private long setupPaidOrder(BigDecimal amount) {
        long orderId = setupCreatedOrder(amount);
        String payKey = "pay-setup-" + UUID.randomUUID();
        LocalDateTime approvedAt = LocalDateTime.now().withNano(0);
        given(paymentGateway.approve(eq(amount), eq(payKey)))
                .willReturn(new ApproveResult("MOCK-PG-" + UUID.randomUUID(), approvedAt));
        orderUseCase.payOrder(new PayOrderCommand(orderId, payKey));
        return orderId;
    }

    private OrderStatus orderStatusOf(long orderId) {
        String status = jdbcTemplate.queryForObject(
                "SELECT order_status FROM purchase_orders WHERE id = ?", String.class, orderId
        );
        return OrderStatus.valueOf(status);
    }
}
