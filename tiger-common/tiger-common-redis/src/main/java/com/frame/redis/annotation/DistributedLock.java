package com.frame.redis.annotation;

import com.frame.redis.enums.LockType;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DistributedLock {

    /**
     * 锁key（支持SPEL）
     */
    String key();

    /**
     * 锁前缀
     */
    String prefix() default "distributed:lock:";

    /**
     * 获取锁等待时间
     */
    long waitTime() default 3;

    /**
     * 锁持有时间（自动续约时为基础时间）
     */
    long leaseTime() default 30;

    TimeUnit timeUnit() default TimeUnit.SECONDS;

    /**
     * 锁类型
     */
    LockType lockType() default LockType.REENTRANT;

    /**
     * 是否开启自动续约（业务执行时间超长时开启）
     */
    boolean autoRenew() default false;

    /**
     * 是否开启防重复提交
     */
    boolean repeatSubmit() default false;

    /**
     * 防重复间隔（秒）
     */
    long repeatInterval() default 2;

    /**
     * 是否使用RedLock（多Redis实例高可用）
     */
    boolean redLock() default false;

    /**
     * 失败提示
     */
    String failMsg() default "系统繁忙，请稍后再试";
}