package com.frame.mq.service;

public interface QueueService {

    /**
     * 发送
     *
     * @param topic   主题
     * @param message 消息
     */
    void send(String topic, String message);
}
