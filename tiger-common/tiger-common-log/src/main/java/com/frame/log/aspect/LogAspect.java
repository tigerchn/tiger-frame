package com.frame.log.aspect;

import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Aspect
@Slf4j
@Component
@ConditionalOnProperty(name = {"log.aspect.enable"}, havingValue = "true", matchIfMissing = true)
public class LogAspect {

    @Pointcut("execution(* com.frame..*.controller.*Controller.*(..)) || execution(* com.frame..*.service.*Service.*(..))")
    private void pointCut() {
    }

    @Around("pointCut()")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        Object[] reqArgs = pjp.getArgs();
        String req = JSON.toJSONString(reqArgs);
        MethodSignature methodSignature = (MethodSignature) pjp.getSignature();
        String methodName = methodSignature.getDeclaringType().getName() + "." + methodSignature.getName();
        log.info("{},request:{}", methodName, req);
        long startTime = System.currentTimeMillis();
        try {
            Object responseObj = pjp.proceed();
            String resp = JSON.toJSONString(responseObj);
            long endTime = System.currentTimeMillis();
            log.info("{},response:{},costTime:{}", methodName, resp, endTime - startTime);
            return responseObj;
        } catch (Throwable e) {
            long endTime = System.currentTimeMillis();
            log.error("{},exception:{},costTime:{}", methodName, e.getMessage(), endTime - startTime, e);
            throw e;
        }
    }

}
