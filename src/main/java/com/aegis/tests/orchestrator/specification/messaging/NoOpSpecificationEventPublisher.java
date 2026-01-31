package com.aegis.tests.orchestrator.specification.messaging;

import com.aegis.tests.orchestrator.specification.dto.event.SpecificationCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * No-op implementation of SpecificationEventPublisher for when messaging is disabled.
 * Used in tests and environments without messaging infrastructure.
 */
@Component
@ConditionalOnProperty(name = "aegis.messaging.provider", havingValue = "none")
public class NoOpSpecificationEventPublisher extends SpecificationEventPublisherBase {

    private static final Logger log = LoggerFactory.getLogger(NoOpSpecificationEventPublisher.class);

    @Override
    public void publishSpecificationCreated(SpecificationCreatedEvent event) {
        log.info("Messaging disabled - Skipping publish for specification ID: {}", event.specificationId());
    }
}

