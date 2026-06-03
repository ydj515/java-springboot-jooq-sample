package org.example.javaspringbootjooqsample.infrastructure.compensation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.javaspringbootjooqsample.application.compensation.payload.PgRefundPayload;
import org.example.javaspringbootjooqsample.domain.compensation.CompensationTask;
import org.example.javaspringbootjooqsample.domain.compensation.CompensationTaskStatus;
import org.example.javaspringbootjooqsample.domain.compensation.CompensationTaskType;
import org.example.javaspringbootjooqsample.domain.compensation.repository.CompensationTaskRepository;
import org.example.javaspringbootjooqsample.domain.payment.gateway.PaymentGateway;
import org.example.javaspringbootjooqsample.domain.payment.gateway.RefundResult;
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
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@Tag("integration-test")
@SpringBootTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class CompensationRetryWorkerMySqlIntegrationTests extends MySqlIntegrationTestSupport {

    private final CompensationRetryWorker worker;
    private final CompensationTaskRepository compensationTaskRepository;
    private final ObjectMapper objectMapper;
    private final JdbcTemplate jdbcTemplate;

    @MockitoBean
    private PaymentGateway paymentGateway;

    CompensationRetryWorkerMySqlIntegrationTests(
            CompensationRetryWorker worker,
            CompensationTaskRepository compensationTaskRepository,
            ObjectMapper objectMapper,
            JdbcTemplate jdbcTemplate
    ) {
        this.worker = worker;
        this.compensationTaskRepository = compensationTaskRepository;
        this.objectMapper = objectMapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Test
    void workerCompletesTaskWhenRefundSucceeds() throws Exception {
        long taskId = insertPendingTask("MOCK-PG-retry-1", new BigDecimal("1000.00"));
        AtomicBoolean refundTxActive = new AtomicBoolean(true);

        given(paymentGateway.refund(eq("MOCK-PG-retry-1"), any()))
                .willAnswer(invocation -> {
                    refundTxActive.set(TransactionSynchronizationManager.isActualTransactionActive());
                    return new RefundResult(LocalDateTime.now().withNano(0));
                });

        worker.runBatch();

        assertThat(refundTxActive).isFalse();
        CompensationTask task = compensationTaskRepository.findById(taskId);
        assertThat(task.getStatus()).isEqualTo(CompensationTaskStatus.SUCCESS);
        assertThat(task.getLastError()).isNull();
    }

    @Test
    void workerMarksTaskFailedAfterMaxRetryExceeded() throws Exception {
        long taskId = insertPendingTask("MOCK-PG-retry-fail", new BigDecimal("2000.00"));
        AtomicBoolean refundTxActive = new AtomicBoolean(true);

        given(paymentGateway.refund(eq("MOCK-PG-retry-fail"), any()))
                .willAnswer(invocation -> {
                    refundTxActive.set(TransactionSynchronizationManager.isActualTransactionActive());
                    throw new RuntimeException("persistent PG failure");
                });

        // 1차 — retry_count 1, status PENDING, nextAttemptAt 미래로 이동
        worker.runBatch();
        resetNextAttempt(taskId);

        // 2차 — retry_count 2
        worker.runBatch();
        resetNextAttempt(taskId);

        // 3차 — MAX_RETRY(3) 도달 → FAILED
        worker.runBatch();

        assertThat(refundTxActive).isFalse();
        CompensationTask task = compensationTaskRepository.findById(taskId);
        assertThat(task.getStatus()).isEqualTo(CompensationTaskStatus.FAILED);
        assertThat(task.getLastError()).contains("max retry exceeded");
    }

    private long insertPendingTask(String paymentKey, BigDecimal amount) throws Exception {
        String payload = objectMapper.writeValueAsString(
                new PgRefundPayload(9999L, paymentKey, amount, "integration test")
        );
        CompensationTask saved = compensationTaskRepository.save(
                CompensationTask.pending(
                        CompensationTaskType.PG_REFUND,
                        payload,
                        LocalDateTime.now().minusSeconds(1)
                )
        );
        return saved.getId();
    }

    private void resetNextAttempt(long taskId) {
        jdbcTemplate.update(
                "UPDATE compensation_tasks SET next_attempt_at = ? WHERE id = ?",
                LocalDateTime.now().minusSeconds(1), taskId
        );
    }
}
