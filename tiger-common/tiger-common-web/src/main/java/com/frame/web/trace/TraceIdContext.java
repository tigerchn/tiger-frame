package com.frame.web.trace;

import org.slf4j.MDC;

import java.util.UUID;

public class TraceIdContext {

    public static final ThreadLocal<String> CURRENT_TRACE_ID = new InheritableThreadLocal<>();

    // 生成 traceId
    public static String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    // 获取 traceId
    public static String getTraceId() {
        return MDC.get(TraceIdConstant.TRACE_ID);
    }

    // 设置 traceId（同时放入 ThreadLocal + MDC）
    public static void setTraceId(String traceId) {
        CURRENT_TRACE_ID.set(traceId);
        MDC.put(TraceIdConstant.TRACE_ID, traceId);
    }

    // 清除（必须调用，防止内存泄漏）
    public static void clearTraceId() {
        CURRENT_TRACE_ID.remove();
        MDC.remove(TraceIdConstant.TRACE_ID);
    }

    // 快捷方法：生成并设置
    public static String createAndSetTraceId() {
        String traceId = generateTraceId();
        setTraceId(traceId);
        return traceId;
    }
}