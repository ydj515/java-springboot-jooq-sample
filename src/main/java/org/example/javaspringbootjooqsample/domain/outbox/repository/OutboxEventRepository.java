package org.example.javaspringbootjooqsample.domain.outbox.repository;

import org.example.javaspringbootjooqsample.domain.outbox.OutboxEvent;

import java.time.LocalDateTime;
import java.util.List;

public interface OutboxEventRepository {
    OutboxEvent save(OutboxEvent event);

    OutboxEvent findById(Long id);

    OutboxEvent findByEventId(String eventId);

    /**
     * PENDING + next_attempt_at <= now 인 row를 limit만큼 SELECT FOR UPDATE SKIP LOCKED로 잠금 후 반환.
     * 동시 publisher 인스턴스가 같은 row를 잡지 않도록 보장.
     */
    List<OutboxEvent> findPendingForUpdate(LocalDateTime now, int limit);
}
