package org.example.javaspringbootjooqsample.config.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.example.javaspringbootjooqsample.common.logging.TraceContext;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MdcLoggingFilter extends OncePerRequestFilter {
    private static final Pattern ID_PATTERN = Pattern.compile("^[A-Za-z0-9._-]{1,128}$");

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        long startTime = System.currentTimeMillis();
        Map<String, String> previousContext = MDC.getCopyOfContextMap();

        String traceId = resolveTraceId(request);
        String requestId = resolveRequestId(request, traceId);

        MDC.put(TraceContext.TRACE_ID_KEY, traceId);
        MDC.put(TraceContext.REQUEST_ID_KEY, requestId);
        MDC.put(TraceContext.HTTP_METHOD_KEY, request.getMethod());
        MDC.put(TraceContext.REQUEST_URI_KEY, request.getRequestURI());
        MDC.put(TraceContext.CLIENT_IP_KEY, resolveClientIp(request));

        response.setHeader(TraceContext.TRACE_ID_HEADER, traceId);
        response.setHeader(TraceContext.REQUEST_ID_HEADER, requestId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            long durationMs = System.currentTimeMillis() - startTime;
            log.info("HTTP request completed status={} durationMs={}", response.getStatus(), durationMs);
            if (previousContext == null || previousContext.isEmpty()) {
                MDC.clear();
            } else {
                MDC.setContextMap(previousContext);
            }
        }
    }

    private String resolveTraceId(HttpServletRequest request) {
        String traceId = request.getHeader(TraceContext.TRACE_ID_HEADER);
        if (!isValidId(traceId)) {
            return generateId();
        }

        return traceId;
    }

    private String resolveRequestId(HttpServletRequest request, String traceId) {
        String requestId = request.getHeader(TraceContext.REQUEST_ID_HEADER);
        if (!isValidId(requestId)) {
            return traceId;
        }

        return requestId;
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor == null || forwardedFor.isBlank()) {
            return request.getRemoteAddr();
        }

        return forwardedFor.split(",")[0].trim();
    }

    private String generateId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private boolean isValidId(String candidate) {
        return candidate != null && ID_PATTERN.matcher(candidate).matches();
    }
}
