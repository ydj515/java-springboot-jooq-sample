package org.example.javaspringbootjooqsample.infrastructure.jooq.listener;

import org.example.javaspringbootjooqsample.infrastructure.jooq.support.JooqQueryCounter;
import org.jooq.ExecuteContext;
import org.jooq.ExecuteListener;
import org.springframework.stereotype.Component;

/**
 * jOOQ 쿼리 실행이 끝날 때마다 JooqQueryCounter를 증가시키는 ExecuteListener 구현체
 * jOOQ 쿼리 실행 횟수를 측정 및 테스트를 위해 적용
 */
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
