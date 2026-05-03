package com.frame.redis.enums;

public enum LockType {
    /**
     * 可重入锁（默认）
     */
    REENTRANT,

    /**
     * 公平锁
     */
    FAIR,

    /**
     * 读锁
     */
    READ,

    /**
     * 写锁
     */
    WRITE
}