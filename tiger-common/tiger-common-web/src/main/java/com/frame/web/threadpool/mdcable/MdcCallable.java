package com.frame.web.threadpool.mdcable;

import com.frame.web.trace.TraceIdContext;

import java.util.concurrent.Callable;

public class MdcCallable<T> extends MdcAbstract implements Callable<T> {

    private final Callable<T> delegate;

    public MdcCallable(Callable<T> delegate) {
        this.traceId = TraceIdContext.getTraceId();
        this.delegate = delegate;
    }

    @Override
    public T call() throws Exception {
        beforeRun();
        try {
            return delegate.call();
        } finally {
            afterRun();
        }
    }
}
