package org.example.javaspringbootjooqsample.application.order;

import org.example.javaspringbootjooqsample.application.order.command.PayOrderCommand;
import org.example.javaspringbootjooqsample.domain.compensation.CompensationTask;
import org.example.javaspringbootjooqsample.domain.compensation.CompensationTaskStatus;
import org.example.javaspringbootjooqsample.domain.compensation.CompensationTaskType;
import org.example.javaspringbootjooqsample.domain.compensation.repository.CompensationTaskRepository;
import org.example.javaspringbootjooqsample.domain.order.OrderStatus;
import org.example.javaspringbootjooqsample.domain.outbox.OutboxEvent;
import org.example.javaspringbootjooqsample.domain.outbox.repository.OutboxEventRepository;
import org.example.javaspringbootjooqsample.domain.payment.Payment;
import org.example.javaspringbootjooqsample.domain.payment.PaymentStatus;
import org.example.javaspringbootjooqsample.domain.payment.exception.PaymentApprovalFailedException;
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
class PayOrderCompensationMySqlIntegrationTests extends MySqlIntegrationTestSupport {

    private final OrderUseCase orderUseCase;
    private final PaymentRepository paymentRepository;
    private final CompensationTaskRepository compensationTaskRepository;
    private final JdbcTemplate jdbcTemplate;

    @MockitoBean
    private PaymentGateway paymentGateway;

    @MockitoBean
    private OutboxEventRepository outboxEventRepository;

    PayOrderCompensationMySqlIntegrationTests(
            OrderUseCase orderUseCase,
            PaymentRepository paymentRepository,
            CompensationTaskRepository compensationTaskRepository,
            JdbcTemplate jdbcTemplate
    ) {
        this.orderUseCase = orderUseCase;
        this.paymentRepository = paymentRepository;
        this.compensationTaskRepository = compensationTaskRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Test
    void scenarioA_pgApproveFailureLeavesPaymentFailedAndOrderCreated() {
        long orderId = setupCreatedOrder(new BigDecimal("10000.00"));
        String key = "scenario-a-" + UUID.randomUUID();
        AtomicBoolean approveTxActive = new AtomicBoolean(true);

        given(paymentGateway.approve(any(), eq(key)))
                .willAnswer(invocation -> {
                    approveTxActive.set(TransactionSynchronizationManager.isActualTransactionActive());
                    throw new PaymentApprovalFailedException("PG declined: insufficient funds");
                });

        assertThatThrownBy(() -> orderUseCase.payOrder(new PayOrderCommand(orderId, key)))
                .isInstanceOf(PaymentApprovalFailedException.class)
                .hasMessageContaining("insufficient funds");

        assertThat(approveTxActive).isFalse();
        assertThat(orderStatusOf(orderId)).isEqualTo(OrderStatus.CREATED);
        Payment payment = paymentRepository.findByIdempotencyKey(key);
        assertThat(payment).isNotNull();
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);

        verify(paymentGateway, never()).refund(any(), any());
        assertThat(compensationTaskRepository.findPendingForUpdate(LocalDateTime.now().plusSeconds(1), 100)).isEmpty();
    }

    @Test
    void scenarioB1_outboxFailureWithRefundSuccessLeavesPaymentRefundedAndOrderCreated() {
        long orderId = setupCreatedOrder(new BigDecimal("20000.00"));
        String key = "scenario-b1-" + UUID.randomUUID();
        LocalDateTime approvedAt = LocalDateTime.now().withNano(0);
        AtomicBoolean approveTxActive = new AtomicBoolean(true);
        AtomicBoolean refundTxActive = new AtomicBoolean(true);

        given(paymentGateway.approve(any(), eq(key)))
                .willAnswer(invocation -> {
                    approveTxActive.set(TransactionSynchronizationManager.isActualTransactionActive());
                    return new ApproveResult("MOCK-PG-b1", approvedAt);
                });
        given(paymentGateway.refund(eq("MOCK-PG-b1"), any()))
                .willAnswer(invocation -> {
                    refundTxActive.set(TransactionSynchronizationManager.isActualTransactionActive());
                    return new RefundResult(approvedAt.plusSeconds(1));
                });
        given(outboxEventRepository.save(any(OutboxEvent.class)))
                .willThrow(new RuntimeException("simulated outbox failure"));

        assertThatThrownBy(() -> orderUseCase.payOrder(new PayOrderCommand(orderId, key)))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("simulated outbox failure");

        assertThat(approveTxActive).isFalse();
        assertThat(refundTxActive).isFalse();
        assertThat(orderStatusOf(orderId)).isEqualTo(OrderStatus.CREATED);
        Payment payment = paymentRepository.findByIdempotencyKey(key);
        assertThat(payment).isNotNull();
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REFUNDED);

        verify(paymentGateway, times(1)).refund(eq("MOCK-PG-b1"), any());
        assertThat(compensationTaskRepository.findPendingForUpdate(LocalDateTime.now().plusSeconds(1), 100)).isEmpty();
    }

    @Test
    void scenarioB2_outboxAndRefundBothFailLeaveCompensationTaskPendingAndPaymentRefundFailed() {
        long orderId = setupCreatedOrder(new BigDecimal("30000.00"));
        String key = "scenario-b2-" + UUID.randomUUID();
        LocalDateTime approvedAt = LocalDateTime.now().withNano(0);
        AtomicBoolean approveTxActive = new AtomicBoolean(true);
        AtomicBoolean refundTxActive = new AtomicBoolean(true);

        given(paymentGateway.approve(any(), eq(key)))
                .willAnswer(invocation -> {
                    approveTxActive.set(TransactionSynchronizationManager.isActualTransactionActive());
                    return new ApproveResult("MOCK-PG-b2", approvedAt);
                });
        given(paymentGateway.refund(eq("MOCK-PG-b2"), any()))
                .willAnswer(invocation -> {
                    refundTxActive.set(TransactionSynchronizationManager.isActualTransactionActive());
                    throw new RuntimeException("simulated PG refund failure");
                });
        given(outboxEventRepository.save(any(OutboxEvent.class)))
                .willThrow(new RuntimeException("simulated outbox failure"));

        assertThatThrownBy(() -> orderUseCase.payOrder(new PayOrderCommand(orderId, key)))
                .isInstanceOf(RuntimeException.class);

        assertThat(approveTxActive).isFalse();
        assertThat(refundTxActive).isFalse();
        assertThat(orderStatusOf(orderId)).isEqualTo(OrderStatus.CREATED);
        Payment payment = paymentRepository.findByIdempotencyKey(key);
        assertThat(payment).isNotNull();
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REFUND_FAILED);

        verify(paymentGateway, times(1)).refund(eq("MOCK-PG-b2"), any());

        List<CompensationTask> tasks = compensationTaskRepository.findPendingForUpdate(LocalDateTime.now().plusSeconds(1), 100);
        assertThat(tasks).hasSize(1);
        CompensationTask task = tasks.get(0);
        assertThat(task.getTaskType()).isEqualTo(CompensationTaskType.PG_REFUND);
        assertThat(task.getStatus()).isEqualTo(CompensationTaskStatus.PENDING);
        assertThat(task.getRetryCount()).isZero();
        assertThat(task.getPayload()).contains("MOCK-PG-b2");
    }

    private long setupCreatedOrder(BigDecimal amount) {
        String customerName = "통합-" + UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO customers (name, email) VALUES (?, ?)",
                customerName, "it@example.com"
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

    private OrderStatus orderStatusOf(long orderId) {
        String status = jdbcTemplate.queryForObject(
                "SELECT order_status FROM purchase_orders WHERE id = ?", String.class, orderId
        );
        return OrderStatus.valueOf(status);
    }
}
