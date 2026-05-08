package com.frame.web.threadpool.impl;

import com.frame.web.threadpool.ThreadPoolTaskDecorator;
import com.frame.web.trace.TraceIdContext;
import org.apache.commons.lang3.StringUtils;

public class TraceIdTaskDecorator implements ThreadPoolTaskDecorator {

    @Override
    public Runnable decorator(Runnable runnable) {
        String traceId = TraceIdContext.getTraceId();
        return () -> {
            if (StringUtils.isNotBlank(traceId)) {
                try {
                    TraceIdContext.setTraceId(traceId);
                    runnable.run();
                } finally {
                    TraceIdContext.clearTraceId();
                }
            } else {
                runnable.run();
            }
        };
    }
}
