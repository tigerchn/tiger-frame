package com.frame.web.threadpool;

import com.frame.web.threadpool.impl.TraceIdTaskDecorator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.List;

@Configuration
@ConditionalOnClass(ThreadPoolTaskExecutor.class)
public class ThreadPoolExecutorAutoConfig {


    @Autowired(required = false)
    private List<ThreadPoolTaskDecorator> threadTaskDecorators;

    @Bean
    public ThreadPoolExecutorBuilder threadPoolTaskExecutorBuilder() {
        return new ThreadPoolExecutorBuilder(threadTaskDecorators);
    }

    @Bean
    public ThreadPoolTaskDecorator TraceIdTaskDecorator() {
        return new TraceIdTaskDecorator();
    }
}
