package org.example.javaspringbootjooqsample.config.async;

import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class MdcTaskDecorator implements TaskDecorator {

    @Override
    public Runnable decorate(Runnable runnable) {
        Map<String, String> callerContext = MDC.getCopyOfContextMap();

        return () -> {
            Map<String, String> previousContext = MDC.getCopyOfContextMap();

            try {
                if (callerContext == null || callerContext.isEmpty()) {
                    MDC.clear();
                } else {
                    MDC.setContextMap(callerContext);
                }

                runnable.run();
            } finally {
                if (previousContext == null || previousContext.isEmpty()) {
                    MDC.clear();
                } else {
                    MDC.setContextMap(previousContext);
                }
            }
        };
    }
}
