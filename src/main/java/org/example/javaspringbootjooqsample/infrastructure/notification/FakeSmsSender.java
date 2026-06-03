package org.example.javaspringbootjooqsample.infrastructure.notification;

import lombok.extern.slf4j.Slf4j;
import org.example.javaspringbootjooqsample.domain.notification.SmsSender;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class FakeSmsSender implements SmsSender {
    @Override
    public void send(String to, String message) {
        log.info("[FAKE SMS] to={} message={}", to, message);
    }
}
