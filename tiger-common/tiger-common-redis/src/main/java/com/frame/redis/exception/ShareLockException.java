package com.frame.redis.exception;

public class ShareLockException extends RuntimeException {

    public ShareLockException(String message) {
        super(message);
    }

    public ShareLockException(String message, Throwable cause) {
        super(message, cause);
    }
}