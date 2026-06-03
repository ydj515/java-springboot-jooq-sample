package org.example.javaspringbootjooqsample.application.compensation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.javaspringbootjooqsample.application.compensation.payload.PgRefundPayload;
import org.example.javaspringbootjooqsample.domain.compensation.CompensationTask;
import org.example.javaspringbootjooqsample.domain.compensation.CompensationTaskType;
import org.example.javaspringbootjooqsample.domain.compensation.repository.CompensationTaskRepository;
import org.example.javaspringbootjooqsample.domain.payment.Payment;
import org.example.javaspringbootjooqsample.domain.payment.PaymentStatus;
import org.example.javaspringbootjooqsample.domain.payment.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CompensationTransactionService {

    private final PaymentRepository paymentRepository;
    private final CompensationTaskRepository compensationTaskRepository;
    private final ObjectMapper objectMapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordRefundSucceeded(Long paymentId, LocalDateTime refundedAt, String reason) {
        Payment payment = paymentRepository.findById(paymentId);
        if (payment == null) {
            return;
        }
        if (payment.getStatus() == PaymentStatus.APPROVED || payment.getStatus() == PaymentStatus.REFUND_FAILED) {
            payment.markRefunded(refundedAt, "compensation refund: " + reason);
            paymentRepository.save(payment);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Long recordRefundFailedAndSchedule(
            Long paymentId,
            String paymentKey,
            BigDecimal amount,
            String reason,
            String errorMessage,
            LocalDateTime now
    ) {
        Payment payment = paymentRepository.findById(paymentId);
        if (payment != null && payment.getStatus() == PaymentStatus.APPROVED) {
            payment.markRefundFailed("compensation refund failed: " + errorMessage, now);
            paymentRepository.save(payment);
        }

        String payload = serialize(new PgRefundPayload(paymentId, paymentKey, amount, reason));
        CompensationTask task = compensationTaskRepository.save(
                CompensationTask.pending(CompensationTaskType.PG_REFUND, payload, now)
        );
        return task.getId();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<Long> claimPendingTasks(LocalDateTime now, int limit, LocalDateTime leaseUntil) {
        List<CompensationTask> batch = compensationTaskRepository.findPendingForUpdate(now, limit);
        if (batch.isEmpty()) {
            return List.of();
        }

        for (CompensationTask task : batch) {
            task.setNextAttemptAt(leaseUntil);
            task.setUpdatedAt(now);
            compensationTaskRepository.save(task);
        }

        return batch.stream()
                .map(CompensationTask::getId)
                .filter(id -> id != null)
                .toList();
    }

    @Transactional(readOnly = true)
    public CompensationTaskSnapshot loadTaskSnapshot(Long taskId) {
        CompensationTask task = compensationTaskRepository.findById(taskId);
        if (task == null) {
            throw new IllegalStateException("compensation task not found: id=" + taskId);
        }
        return new CompensationTaskSnapshot(
                task.getId(),
                task.getTaskType(),
                task.getStatus(),
                task.getRetryCount(),
                task.getPayload()
        );
    }

    @Transactional(readOnly = true)
    public boolean isPaymentRefunded(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId);
        return payment != null && payment.getStatus() == PaymentStatus.REFUNDED;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordTaskRefundSucceeded(
            Long taskId,
            Long paymentId,
            LocalDateTime refundedAt,
            LocalDateTime now
    ) {
        CompensationTask task = compensationTaskRepository.findById(taskId);
        if (task == null) {
            throw new IllegalStateException("compensation task not found: id=" + taskId);
        }

        Payment payment = paymentRepository.findById(paymentId);
        if (payment != null
                && (payment.getStatus() == PaymentStatus.APPROVED
                || payment.getStatus() == PaymentStatus.REFUND_FAILED)) {
            payment.markRefunded(refundedAt, "compensation retry succeeded");
            paymentRepository.save(payment);
        }

        task.markSuccess(now);
        compensationTaskRepository.save(task);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public CompensationTaskSnapshot recordTaskRefundFailure(
            Long taskId,
            String errorMessage,
            LocalDateTime nextAttempt,
            LocalDateTime now
    ) {
        CompensationTask task = compensationTaskRepository.findById(taskId);
        if (task == null) {
            throw new IllegalStateException("compensation task not found: id=" + taskId);
        }

        if (nextAttempt == null) {
            task.markFailed("max retry exceeded: " + errorMessage, now);
        } else {
            task.markRetry(errorMessage, nextAttempt, now);
        }
        compensationTaskRepository.save(task);

        return new CompensationTaskSnapshot(
                task.getId(),
                task.getTaskType(),
                task.getStatus(),
                task.getRetryCount(),
                task.getPayload()
        );
    }

    private String serialize(PgRefundPayload payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("failed to serialize compensation payload", e);
        }
    }
}
