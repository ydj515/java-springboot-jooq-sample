package org.example.javaspringbootjooqsample.infrastructure.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.javaspringbootjooqsample.domain.order.event.OrderPaidEvent;
import org.example.javaspringbootjooqsample.domain.outbox.OutboxEvent;
import org.example.javaspringbootjooqsample.domain.outbox.repository.OutboxEventRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxPublisher {

    private static final int BATCH_SIZE = 10;
    private static final int MAX_RETRY = 5;
    private static final long MAX_BACKOFF_SECONDS = 60L;

    private final OutboxEventRepository outboxEventRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelayString = "${app.outbox.publisher.fixed-delay-ms:1000}")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void publishBatch() {
        LocalDateTime now = LocalDateTime.now();
        List<OutboxEvent> batch = outboxEventRepository.findPendingForUpdate(now, BATCH_SIZE);
        if (batch.isEmpty()) {
            return;
        }

        for (OutboxEvent event : batch) {
            try {
                dispatch(event);
                event.markPublished(LocalDateTime.now());
            } catch (Exception e) {
                handleFailure(event, e);
            }
            outboxEventRepository.save(event);
        }
    }

    private void dispatch(OutboxEvent event) throws Exception {
        Object deserialized = switch (event.getEventType()) {
            case OrderPaidEvent.EVENT_TYPE -> objectMapper.readValue(event.getPayload(), OrderPaidEvent.class);
            default -> throw new IllegalStateException("unknown event type: " + event.getEventType());
        };
        applicationEventPublisher.publishEvent(deserialized);
        log.info("outbox published: id={} type={} eventId={}", event.getId(), event.getEventType(), event.getEventId());
    }

    private void handleFailure(OutboxEvent event, Exception e) {
        long backoff = backoffSeconds(event.getRetryCount() + 1);
        LocalDateTime nextAttempt = LocalDateTime.now().plusSeconds(backoff);
        String reason = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
        event.markRetry(reason, nextAttempt);
        if (event.getRetryCount() >= MAX_RETRY) {
            event.markFailed("max retry exceeded: " + reason);
        }
        log.warn("outbox publish failed: id={} retry={} reason={}", event.getId(), event.getRetryCount(), reason);
    }

    private long backoffSeconds(int attempt) {
        int shift = Math.min(Math.max(attempt - 1, 0), 8);
        long candidate = 2L << shift;
        return Math.min(MAX_BACKOFF_SECONDS, candidate);
    }
}
