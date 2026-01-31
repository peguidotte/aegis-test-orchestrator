package com.aegis.tests.orchestrator.specification.messaging;

import com.aegis.tests.orchestrator.shared.config.RabbitMQConfig;
import com.aegis.tests.orchestrator.specification.dto.event.SpecificationCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "spring.rabbitmq.enabled", havingValue = "true", matchIfMissing = true)
public class SpecificationEventPublisher extends SpecificationEventPublisherBase {

    private static final Logger log = LoggerFactory.getLogger(SpecificationEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public SpecificationEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publishSpecificationCreated(SpecificationCreatedEvent event) {
        log.info("Publishing SpecificationCreatedEvent for specification ID: {}", event.specificationId());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.TEST_GENERATION_EXCHANGE,
                RabbitMQConfig.TEST_GENERATION_ROUTING_KEY,
                event
        );
        log.debug("SpecificationCreatedEvent published successfully: {}", event);
    }
}

