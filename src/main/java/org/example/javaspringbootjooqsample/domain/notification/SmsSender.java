package org.example.javaspringbootjooqsample.domain.notification;

public interface SmsSender {
    void send(String to, String message);
}
