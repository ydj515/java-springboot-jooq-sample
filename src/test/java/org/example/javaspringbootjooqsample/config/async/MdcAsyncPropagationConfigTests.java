package org.example.javaspringbootjooqsample.config.async;

import org.example.javaspringbootjooqsample.common.logging.TraceContext;
import org.example.javaspringbootjooqsample.support.MySqlSpringBootTestSupport;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Async;
import org.springframework.test.context.TestConstructor;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Tag("config")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class MdcAsyncPropagationConfigTests extends MySqlSpringBootTestSupport {

    private final Executor taskExecutor;

    private final AsyncMdcProbe asyncMdcProbe;

    MdcAsyncPropagationConfigTests(
            @Qualifier("taskExecutor") Executor taskExecutor,
            AsyncMdcProbe asyncMdcProbe
    ) {
        this.taskExecutor = taskExecutor;
        this.asyncMdcProbe = asyncMdcProbe;
    }

    @Test
    void propagatesMdcContextToAsyncMethod() throws Exception {
        // given
        MDC.put(TraceContext.TRACE_ID_KEY, "trace-async-method");
        MDC.put(TraceContext.REQUEST_ID_KEY, "request-async-method");

        try {
            // when
            MdcSnapshot snapshot = asyncMdcProbe.capture().get(5, TimeUnit.SECONDS);

            // then
            assertThat(snapshot.traceId()).isEqualTo("trace-async-method");
            assertThat(snapshot.requestId()).isEqualTo("request-async-method");
        } finally {
            MDC.clear();
        }
    }

    @Test
    void propagatesMdcContextToTaskExecutor() throws Exception {
        // given
        MDC.put(TraceContext.TRACE_ID_KEY, "trace-task-executor");
        MDC.put(TraceContext.REQUEST_ID_KEY, "request-task-executor");

        try {
            CompletableFuture<MdcSnapshot> future = new CompletableFuture<>();

            // when
            taskExecutor.execute(() -> future.complete(MdcSnapshot.capture()));

            MdcSnapshot snapshot = future.get(5, TimeUnit.SECONDS);

            // then
            assertThat(snapshot.traceId()).isEqualTo("trace-task-executor");
            assertThat(snapshot.requestId()).isEqualTo("request-task-executor");
        } finally {
            MDC.clear();
        }
    }

    record MdcSnapshot(String traceId, String requestId) {
        static MdcSnapshot capture() {
            return new MdcSnapshot(TraceContext.getTraceId(), TraceContext.getRequestId());
        }
    }

    static class AsyncMdcProbe {

        @Async
        public CompletableFuture<MdcSnapshot> capture() {
            return CompletableFuture.completedFuture(MdcSnapshot.capture());
        }
    }

    @TestConfiguration
    static class AsyncMdcProbeConfiguration {

        @Bean
        AsyncMdcProbe asyncMdcProbe() {
            return new AsyncMdcProbe();
        }
    }
}
