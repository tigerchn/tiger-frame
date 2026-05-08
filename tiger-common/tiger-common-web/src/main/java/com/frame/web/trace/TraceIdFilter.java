package com.frame.web.trace;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;

@Component
@Slf4j
public class TraceIdFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;


        try {
            String traceId = httpRequest.getHeader(TraceIdConstant.TRACE_ID);

            if (StringUtils.hasText(traceId)) {
                // 生成 traceId
                traceId = TraceIdContext.generateTraceId();
            }

            TraceIdContext.setTraceId(traceId);

            chain.doFilter(httpRequest, response);
        } finally {
            // 必须清理
            TraceIdContext.clearTraceId();
        }
    }
}