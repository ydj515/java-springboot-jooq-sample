package org.example.javaspringbootjooqsample.infrastructure.jooq.support;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
public class JooqQueryCounter {

    private final AtomicInteger count = new AtomicInteger();

    public void increment() {
        count.incrementAndGet();
    }

    public int getCount() {
        return count.get();
    }

    public void reset() {
        count.set(0);
    }
}
