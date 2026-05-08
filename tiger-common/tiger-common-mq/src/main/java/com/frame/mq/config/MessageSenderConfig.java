package com.frame.mq.config;

import com.frame.mq.service.QueueService;
import com.frame.mq.service.impl.RabbitMQMessageSender;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

public class MessageSenderConfig {

    @Bean
    @ConditionalOnProperty(name = "spring.rabbitmq.host")
    public QueueService rabbitMQMessageSender(AmqpTemplate amqpTemplate) {
        return new RabbitMQMessageSender(amqpTemplate);
    }
}