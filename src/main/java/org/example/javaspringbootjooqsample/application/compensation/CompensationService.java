package org.example.javaspringbootjooqsample.application.compensation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.javaspringbootjooqsample.application.compensation.payload.PgRefundPayload;
import org.example.javaspringbootjooqsample.domain.compensation.CompensationTaskStatus;
import org.example.javaspringbootjooqsample.domain.compensation.CompensationTaskType;
import org.example.javaspringbootjooqsample.domain.payment.gateway.PaymentGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CompensationService {

    public static final int MAX_RETRY = 3;
    public static final long MAX_BACKOFF_SECONDS = 60L;

    private static final Logger log = LoggerFactory.getLogger(CompensationService.class);

    private final PaymentGateway paymentGateway;
    private final CompensationTransactionService compensationTransactionService;
    private final ObjectMapper objectMapper;

    /**
     * PG 환불은 DB 트랜잭션 밖에서 수행한다.
     * 환불 결과 기록과 재시도 task 저장만 별도의 짧은 트랜잭션으로 처리한다.
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public CompensationOutcome compensateApprovedPayment(
            Long paymentId,
            String paymentKey,
            BigDecimal amount,
            String reason,
            LocalDateTime now
    ) {
        try {
            var result = paymentGateway.refund(paymentKey, amount);
            compensationTransactionService.recordRefundSucceeded(paymentId, result.refundedAt(), reason);
            log.info("compensation refund succeeded: paymentId={} reason={}", paymentId, reason);
            return new CompensationOutcome.Refunded(result.refundedAt());
        } catch (Exception e) {
            Long taskId = compensationTransactionService.recordRefundFailedAndSchedule(
                    paymentId,
                    paymentKey,
                    amount,
                    reason,
                    errorMessage(e),
                    now
            );
            log.warn(
                    "compensation refund failed, task scheduled: paymentId={} taskId={} error={}",
                    paymentId, taskId, e.getMessage()
            );
            return new CompensationOutcome.Scheduled(taskId);
        }
    }

    /**
     * CompensationRetryWorker가 PENDING task를 위해 호출.
     * task claim은 worker에서 먼저 짧은 트랜잭션으로 끝내고, PG 환불은 여기서 트랜잭션 없이 수행한다.
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void processCompensationTask(Long taskId, LocalDateTime now) {
        CompensationTaskSnapshot task = compensationTransactionService.loadTaskSnapshot(taskId);
        if (task.status() != CompensationTaskStatus.PENDING) {
            return;
        }

        if (task.taskType() == CompensationTaskType.PG_REFUND) {
            retryPgRefund(task, now);
        }
    }

    private void retryPgRefund(CompensationTaskSnapshot task, LocalDateTime now) {
        PgRefundPayload payload = deserialize(task.payload());

        if (compensationTransactionService.isPaymentRefunded(payload.paymentId())) {
            compensationTransactionService.recordTaskRefundSucceeded(
                    task.id(),
                    payload.paymentId(),
                    now,
                    now
            );
            log.info("compensation task short-circuit (already refunded): taskId={}", task.id());
            return;
        }

        try {
            var result = paymentGateway.refund(payload.paymentKey(), payload.amount());
            compensationTransactionService.recordTaskRefundSucceeded(
                    task.id(),
                    payload.paymentId(),
                    result.refundedAt(),
                    now
            );
            log.info("compensation task succeeded: taskId={}", task.id());
        } catch (Exception e) {
            int nextRetry = task.retryCount() + 1;
            if (nextRetry >= MAX_RETRY) {
                compensationTransactionService.recordTaskRefundFailure(
                        task.id(),
                        errorMessage(e),
                        null,
                        now
                );
                log.error(
                        "compensation task FAILED after max retry: taskId={} paymentId={} error={}",
                        task.id(), payload.paymentId(), e.getMessage()
                );
                return;
            }

            long backoffSeconds = backoffSeconds(nextRetry);
            compensationTransactionService.recordTaskRefundFailure(
                    task.id(),
                    errorMessage(e),
                    now.plusSeconds(backoffSeconds),
                    now
            );
            log.warn(
                    "compensation task retry scheduled: taskId={} retryCount={} nextAttemptInSec={} error={}",
                    task.id(), nextRetry, backoffSeconds, e.getMessage()
            );
        }
    }

    private long backoffSeconds(int retryCount) {
        long seconds = 1L << retryCount;
        return Math.min(seconds, MAX_BACKOFF_SECONDS);
    }

    private String errorMessage(Exception e) {
        return e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
    }

    private PgRefundPayload deserialize(String payload) {
        try {
            return objectMapper.readValue(payload, PgRefundPayload.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("failed to deserialize compensation payload", e);
        }
    }
}
