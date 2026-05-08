package com.frame.web.threadpool.mdcable;

import com.frame.web.trace.TraceIdContext;
import org.slf4j.MDC;

public class MdcAbstract {

    protected String traceId;

    protected void beforeRun() {
        TraceIdContext.setTraceId(traceId);
    }

    protected void afterRun() {
        TraceIdContext.clearTraceId();
        MDC.clear();
    }
}
