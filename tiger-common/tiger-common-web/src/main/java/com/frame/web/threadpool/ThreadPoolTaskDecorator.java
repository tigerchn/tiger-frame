package com.frame.web.threadpool;

public interface ThreadPoolTaskDecorator {
    Runnable decorator(Runnable runnable);
}
