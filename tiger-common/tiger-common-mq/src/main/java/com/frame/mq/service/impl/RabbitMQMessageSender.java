package com.frame.mq.service.impl;

import com.frame.mq.service.QueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.AmqpTemplate;

@RequiredArgsConstructor
public class RabbitMQMessageSender implements QueueService {

    private final AmqpTemplate amqpTemplate;

    @Override
    public void send(String topic, String message) {
        amqpTemplate.convertAndSend(topic, message);
    }
}
