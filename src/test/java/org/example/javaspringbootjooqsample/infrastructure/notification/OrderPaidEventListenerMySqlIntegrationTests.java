package org.example.javaspringbootjooqsample.infrastructure.notification;

import org.example.javaspringbootjooqsample.domain.notification.SmsSender;
import org.example.javaspringbootjooqsample.domain.order.event.OrderPaidEvent;
import org.example.javaspringbootjooqsample.support.MySqlIntegrationTestSupport;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.TestConstructor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@Tag("integration-test")
@SpringBootTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class OrderPaidEventListenerMySqlIntegrationTests extends MySqlIntegrationTestSupport {

    private static final String CONSUMER_NAME = "order-paid-sms-sender";

    private final ApplicationEventPublisher eventPublisher;
    private final ProcessedEventJooqRepositoryAdapter processedEventRepository;
    private final TransactionTemplate transactionTemplate;

    @MockitoBean
    private SmsSender smsSender;

    OrderPaidEventListenerMySqlIntegrationTests(
            ApplicationEventPublisher eventPublisher,
            ProcessedEventJooqRepositoryAdapter processedEventRepository,
            PlatformTransactionManager transactionManager
    ) {
        this.eventPublisher = eventPublisher;
        this.processedEventRepository = processedEventRepository;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Test
    void publishingOrderPaidEventOnceSendsSmsAndStoresProcessedEvent() {
        OrderPaidEvent event = sampleEvent();

        publishInTransaction(event);

        verify(smsSender, times(1)).send(
                eq("buyer-of-order-" + event.orderId()),
                contains(event.paymentKey())
        );
        assertThat(processedEventRepository.existsByEventIdAndConsumerName(event.eventId(), CONSUMER_NAME)).isTrue();
    }

    @Test
    void publishingSameEventTwiceSendsSmsOnlyOnce() {
        OrderPaidEvent event = sampleEvent();

        publishInTransaction(event);
        publishInTransaction(event);

        verify(smsSender, times(1)).send(any(), any());
        verifyNoMoreInteractions(smsSender);
    }

    private void publishInTransaction(OrderPaidEvent event) {
        transactionTemplate.executeWithoutResult(status -> eventPublisher.publishEvent(event));
    }

    private OrderPaidEvent sampleEvent() {
        return new OrderPaidEvent(
                UUID.randomUUID().toString(),
                999L,
                99L,
                "MOCK-PG-" + UUID.randomUUID(),
                new BigDecimal("12345.00"),
                LocalDateTime.now()
        );
    }
}
