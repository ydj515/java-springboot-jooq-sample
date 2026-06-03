package org.example.javaspringbootjooqsample.infrastructure.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.javaspringbootjooqsample.domain.notification.SmsSender;
import org.example.javaspringbootjooqsample.domain.order.event.OrderPaidEvent;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderPaidEventListener {

    private static final String CONSUMER_NAME = "order-paid-sms-sender";

    private final ProcessedEventJooqRepositoryAdapter processedEventRepository;
    private final SmsSender smsSender;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handle(OrderPaidEvent event) {
        if (processedEventRepository.existsByEventIdAndConsumerName(event.eventId(), CONSUMER_NAME)) {
            log.info("event already processed: eventId={} consumer={}", event.eventId(), CONSUMER_NAME);
            return;
        }

        try {
            processedEventRepository.insertEvent(
                    ProcessedEvent.builder()
                            .eventId(event.eventId())
                            .consumerName(CONSUMER_NAME)
                            .processedAt(LocalDateTime.now())
                            .build()
            );
        } catch (DataIntegrityViolationException e) {
            log.info("event processed concurrently: eventId={} consumer={}", event.eventId(), CONSUMER_NAME);
            return;
        }

        smsSender.send(
                "buyer-of-order-" + event.orderId(),
                "주문 " + event.orderId() + " 결제가 완료되었습니다. (paymentKey=" + event.paymentKey() + ")"
        );
    }
}
