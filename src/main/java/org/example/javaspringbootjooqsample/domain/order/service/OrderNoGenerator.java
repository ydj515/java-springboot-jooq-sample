package org.example.javaspringbootjooqsample.domain.order.service;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

@Component
public class OrderNoGenerator {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final int RANDOM_LENGTH = 10;

    public String generate() {
        String datePart = LocalDate.now().format(DATE_FORMATTER);
        String randomPart = UUID.randomUUID().toString()
                .replace("-", "")
                .substring(0, RANDOM_LENGTH)
                .toUpperCase(Locale.ROOT);
        return "ORD-" + datePart + "-" + randomPart;
    }
}
