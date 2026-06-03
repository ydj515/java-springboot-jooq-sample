package org.example.javaspringbootjooqsample.common.logging;

import org.slf4j.MDC;

public final class TraceContext {
    public static final String TRACE_ID_KEY = "traceId";
    public static final String REQUEST_ID_KEY = "requestId";
    public static final String HTTP_METHOD_KEY = "httpMethod";
    public static final String REQUEST_URI_KEY = "requestUri";
    public static final String CLIENT_IP_KEY = "clientIp";

    public static final String TRACE_ID_HEADER = "X-Trace-Id";
    public static final String REQUEST_ID_HEADER = "X-Request-Id";

    private TraceContext() {
    }

    public static String getTraceId() {
        return MDC.get(TRACE_ID_KEY);
    }

    public static String getRequestId() {
        return MDC.get(REQUEST_ID_KEY);
    }
}
