package com.aegis.tests.orchestrator.services.messaging;

import com.aegis.tests.orchestrator.model.dto.SpecificationCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * No-op implementation of SpecificationEventPublisher for when RabbitMQ is disabled.
 * Used in tests and environments without RabbitMQ.
 */
@Component
@ConditionalOnProperty(name = "spring.rabbitmq.enabled", havingValue = "false")
public class NoOpSpecificationEventPublisher extends SpecificationEventPublisherBase {

    private static final Logger log = LoggerFactory.getLogger(NoOpSpecificationEventPublisher.class);

    @Override
    public void publishSpecificationCreated(SpecificationCreatedEvent event) {
        log.info("RabbitMQ disabled - Skipping publish for specification ID: {}", event.specificationId());
    }
}

