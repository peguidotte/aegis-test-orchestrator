package com.aegis.tests.orchestrator.specification.messaging;

import com.aegis.tests.orchestrator.shared.config.PubSubMessagingProperties;
import com.aegis.tests.orchestrator.specification.dto.event.SpecificationCreatedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * GCP Pub/Sub implementation of SpecificationEventPublisher.
 * Publishes specification events to configured Pub/Sub topics.
 */
@Component
public class PubSubSpecificationEventPublisher extends SpecificationEventPublisherBase {

    private static final Logger log = LoggerFactory.getLogger(PubSubSpecificationEventPublisher.class);

    private final PubSubTemplate pubSubTemplate;
    private final PubSubMessagingProperties properties;
    private final ObjectMapper objectMapper;

    public PubSubSpecificationEventPublisher(PubSubTemplate pubSubTemplate, PubSubMessagingProperties properties, ObjectMapper objectMapper) {
        this.pubSubTemplate = pubSubTemplate;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publishSpecificationCreated(SpecificationCreatedEvent event) {
        String topic = properties.getTestGenerationRequestedTopic();
        log.info("Publishing SpecificationCreatedEvent to Pub/Sub topic '{}' for specification ID: {}",
                topic, event.specificationId());

        try {
            String payload = objectMapper.writeValueAsString(event);
            pubSubTemplate.publish(topic, payload);
            log.debug("Successfully published event to Pub/Sub topic '{}'", topic);
        } catch (JsonProcessingException | RuntimeException e) {
            log.error("Failed to publish SpecificationCreatedEvent to Pub/Sub topic '{}'", topic, e);
            throw new RuntimeException("Failed to publish event to Pub/Sub", e);
        }
    }
}
