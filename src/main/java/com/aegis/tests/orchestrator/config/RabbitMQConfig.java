package com.aegis.tests.orchestrator.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "spring.rabbitmq.enabled", havingValue = "true", matchIfMissing = true)
public class RabbitMQConfig {

    public static final String SPECIFICATION_QUEUE = "aegis.specification.created";
    public static final String SPECIFICATION_EXCHANGE = "aegis.specification.exchange";
    public static final String SPECIFICATION_ROUTING_KEY = "specification.created";

    @Bean
    Queue specificationQueue() {
        return new Queue(SPECIFICATION_QUEUE, true);
    }

    @Bean
    DirectExchange specificationExchange() {
        return new DirectExchange(SPECIFICATION_EXCHANGE);
    }

    @Bean
    Binding specificationBinding(Queue specificationQueue, DirectExchange specificationExchange) {
        return BindingBuilder
                .bind(specificationQueue)
                .to(specificationExchange)
                .with(SPECIFICATION_ROUTING_KEY);
    }

    @Bean
    MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter);
        return rabbitTemplate;
    }
}

