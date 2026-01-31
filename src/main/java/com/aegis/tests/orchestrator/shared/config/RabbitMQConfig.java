package com.aegis.tests.orchestrator.shared.config;

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

    public static final String TEST_GENERATION_QUEUE = "aegis-test.test-generation.started";
    public static final String TEST_GENERATION_EXCHANGE = "aegis-test.test-generation.exchange";
    public static final String TEST_GENERATION_ROUTING_KEY = "specification.started";

    @Bean
    Queue specificationQueue() {
        return new Queue(TEST_GENERATION_QUEUE, true);
    }

    @Bean
    DirectExchange specificationExchange() {
        return new DirectExchange(TEST_GENERATION_EXCHANGE);
    }

    @Bean
    Binding specificationBinding(Queue specificationQueue, DirectExchange specificationExchange) {
        return BindingBuilder
                .bind(specificationQueue)
                .to(specificationExchange)
                .with(TEST_GENERATION_ROUTING_KEY);
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

