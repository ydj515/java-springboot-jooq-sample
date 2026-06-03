package org.example.javaspringbootjooqsample.infrastructure.compensation;

import lombok.RequiredArgsConstructor;
import org.example.javaspringbootjooqsample.application.compensation.CompensationService;
import org.example.javaspringbootjooqsample.application.compensation.CompensationTransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * PENDING 보상 task를 polling하여 CompensationService에 위임.
 * - claim 트랜잭션: SKIP LOCKED로 batch를 짧게 잠그고 nextAttemptAt을 lease 시각으로 민다.
 * - PG refund 재시도: claim 트랜잭션이 끝난 뒤 수행한다.
 * - 결과 기록: CompensationService 내부의 짧은 트랜잭션에 위임한다.
 */
@Component
@RequiredArgsConstructor
public class CompensationRetryWorker {

    private static final Logger log = LoggerFactory.getLogger(CompensationRetryWorker.class);
    private static final int BATCH_SIZE = 10;
    private static final long CLAIM_LEASE_SECONDS = 300L;

    private final CompensationTransactionService compensationTransactionService;
    private final CompensationService compensationService;

    @Scheduled(fixedDelayString = "${app.compensation.worker.fixed-delay-ms:1000}")
    public void runBatch() {
        LocalDateTime now = LocalDateTime.now();
        List<Long> taskIds = compensationTransactionService.claimPendingTasks(
                now,
                BATCH_SIZE,
                now.plusSeconds(CLAIM_LEASE_SECONDS)
        );
        if (taskIds.isEmpty()) {
            return;
        }

        log.debug("compensation worker batch picked: size={}", taskIds.size());
        for (Long taskId : taskIds) {
            try {
                compensationService.processCompensationTask(taskId, now);
            } catch (Exception e) {
                log.error("compensation task processing crashed: taskId={} error={}", taskId, e.getMessage(), e);
            }
        }
    }
}
