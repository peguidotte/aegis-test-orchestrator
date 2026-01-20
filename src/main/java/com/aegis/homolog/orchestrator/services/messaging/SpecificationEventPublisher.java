package com.aegis.homolog.orchestrator.services.messaging;

import com.aegis.homolog.orchestrator.config.RabbitMQConfig;
import com.aegis.homolog.orchestrator.model.dto.SpecificationCreatedEvent;
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
                RabbitMQConfig.SPECIFICATION_EXCHANGE,
                RabbitMQConfig.SPECIFICATION_ROUTING_KEY,
                event
        );
        log.debug("SpecificationCreatedEvent published successfully: {}", event);
    }
}

