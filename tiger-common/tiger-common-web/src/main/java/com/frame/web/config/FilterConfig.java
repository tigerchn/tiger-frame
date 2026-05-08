package com.frame.web.config;

import com.frame.web.trace.TraceIdFilter;
import jakarta.annotation.Resource;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;


@Configuration
public class FilterConfig {

    @Resource
    private TraceIdFilter traceIdFilter;

    @Bean
    public FilterRegistrationBean<TraceIdFilter> registerTraceFilter() {
        FilterRegistrationBean<TraceIdFilter> registration = new FilterRegistrationBean<>();

        registration.setFilter(traceIdFilter);
        registration.addUrlPatterns("/*");
        registration.setName("traceIdFilter");

        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);

        return registration;
    }

}
