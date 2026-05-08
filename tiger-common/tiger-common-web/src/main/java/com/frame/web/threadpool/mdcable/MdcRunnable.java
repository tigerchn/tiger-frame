package com.frame.web.threadpool.mdcable;

import com.frame.web.trace.TraceIdContext;

public class MdcRunnable extends MdcAbstract implements Runnable {

    private final Runnable runnable;

    public MdcRunnable(Runnable runnable) {
        this.traceId = TraceIdContext.getTraceId();
        this.runnable = runnable;
    }

    @Override
    public void run() {
        beforeRun();
        try {
            runnable.run();
        } finally {
            afterRun();
        }
    }
}
