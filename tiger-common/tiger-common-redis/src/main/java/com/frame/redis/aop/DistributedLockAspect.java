package com.frame.redis.aop;

import com.frame.redis.annotation.DistributedLock;
import com.frame.redis.enums.LockType;
import com.frame.redis.exception.ShareLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class DistributedLockAspect {

    private final RedissonClient redissonClient;
    private final ExpressionParser parser = new SpelExpressionParser();
    private final DefaultParameterNameDiscoverer nameDiscoverer = new DefaultParameterNameDiscoverer();

    @Around("@annotation(lock)")
    public Object around(ProceedingJoinPoint joinPoint, DistributedLock lock) throws Throwable {
        String lockKey = getLockKey(joinPoint, lock);
        RLock rLock = getRLock(lockKey, lock.lockType());

        // 1. 防重复提交
        if (lock.repeatSubmit()) {
            doRepeatSubmit(lockKey, lock);
        }

        boolean lockSuccess = false;

        try {
            log.info("[分布式锁] 尝试获取锁 key:{}", lockKey);
            // 3. 自动续约模式 / 普通模式
            if (lock.autoRenew()) {
                lockSuccess = rLock.tryLock(lock.waitTime(), -1, lock.timeUnit());
            } else {
                lockSuccess = rLock.tryLock(lock.waitTime(), lock.leaseTime(), lock.timeUnit());
            }

            if (!lockSuccess) {
                throw new ShareLockException(lock.failMsg());
            }
            log.info("[分布式锁] 获取成功 key:{}", lockKey);
            return joinPoint.proceed();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ShareLockException("获取锁被中断", e);
        } finally {
            if (lockSuccess && rLock.isHeldByCurrentThread()) {
                rLock.unlock();
                log.info("[分布式锁] 释放成功 key:{}", lockKey);
            }
        }
    }

    /**
     * 防重复提交
     */
    private void doRepeatSubmit(String lockKey, DistributedLock lock) {
        String repeatKey = "repeat:submit:" + lockKey;
        RLock repeatLock = redissonClient.getLock(repeatKey);
        try {
            boolean success = repeatLock.tryLock(0, lock.repeatInterval(), lock.timeUnit());
            if (!success) {
                throw new ShareLockException("请勿重复提交");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ShareLockException("重复提交校验异常");
        }
    }

    /**
     * 获取锁实例
     */
    private RLock getRLock(String key, LockType type) {
        return switch (type) {
            case FAIR -> redissonClient.getFairLock(key);
            case READ -> redissonClient.getReadWriteLock(key).readLock();
            case WRITE -> redissonClient.getReadWriteLock(key).writeLock();
            default -> redissonClient.getLock(key);
        };
    }

    /**
     * SPEL解析key
     */
    private String getLockKey(ProceedingJoinPoint joinPoint, DistributedLock lock) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        var method = signature.getMethod();
        var args = joinPoint.getArgs();
        var context = new StandardEvaluationContext();
        String[] paramNames = nameDiscoverer.getParameterNames(method);
        if (paramNames != null) {
            for (int i = 0; i < paramNames.length; i++) {
                context.setVariable(paramNames[i], args[i]);
            }
        }
        String value = parser.parseExpression(lock.key()).getValue(context, String.class);
        return lock.prefix() + value;
    }
}