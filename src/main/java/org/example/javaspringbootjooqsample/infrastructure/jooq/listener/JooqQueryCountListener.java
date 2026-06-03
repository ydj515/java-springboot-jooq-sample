package org.example.javaspringbootjooqsample.infrastructure.jooq.listener;

import org.example.javaspringbootjooqsample.infrastructure.jooq.support.JooqQueryCounter;
import org.jooq.ExecuteContext;
import org.jooq.ExecuteListener;
import org.springframework.stereotype.Component;

@Component
public class JooqQueryCountListener implements ExecuteListener {

    private final JooqQueryCounter counter;

    public JooqQueryCountListener(JooqQueryCounter counter) {
        this.counter = counter;
    }

    @Override
    public void executeEnd(ExecuteContext ctx) {
        counter.increment();
    }
}
