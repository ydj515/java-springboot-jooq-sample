package org.example.javaspringbootjooqsample.domain.compensation.repository;

import org.example.javaspringbootjooqsample.domain.compensation.CompensationTask;

import java.time.LocalDateTime;
import java.util.List;

public interface CompensationTaskRepository {
    CompensationTask save(CompensationTask task);

    CompensationTask findById(Long id);

    /**
     * PENDING + next_attempt_at <= now 인 task를 limit만큼 SELECT FOR UPDATE SKIP LOCKED로 잠금 후 반환.
     * 동시 worker 인스턴스가 같은 row를 잡지 않도록 보장 (MySQL 8+).
     */
    List<CompensationTask> findPendingForUpdate(LocalDateTime now, int limit);
}
