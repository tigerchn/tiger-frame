package com.frame.web.interceptor;

import com.frame.web.trace.TraceIdConstant;
import com.frame.web.trace.TraceIdContext;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.support.HttpRequestWrapper;

import java.io.IOException;


public class RestTemplateInterceptor implements ClientHttpRequestInterceptor {

    public RestTemplateInterceptor() {
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest httpRequest, byte[] bytes, ClientHttpRequestExecution clientHttpRequestExecution) throws IOException {
        return clientHttpRequestExecution.execute(process(httpRequest), bytes);
    }

    protected HttpRequestWrapper process(HttpRequest httpRequest) {
        HttpRequestWrapper httpRequestWrapper = new HttpRequestWrapper(httpRequest);
        String traceId = TraceIdContext.getTraceId();
        /** traceId 不存在时不需要随机生成一个，被调用的服务的Servlet filter会生成一个，详见TraceIdFilter */
        if (StringUtils.isNotBlank(traceId)) {
            httpRequestWrapper.getHeaders().set(TraceIdConstant.TRACE_ID, traceId);
        }
        return httpRequestWrapper;
    }

}
