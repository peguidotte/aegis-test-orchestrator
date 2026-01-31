package com.aegis.tests.orchestrator.specification.messaging;

import com.aegis.tests.orchestrator.shared.config.PubSubMessagingProperties;
import com.aegis.tests.orchestrator.specification.dto.event.SpecificationCreatedEvent;
import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * GCP Pub/Sub implementation of SpecificationEventPublisher.
 * Publishes specification events to configured Pub/Sub topics.
 */
@Component
@ConditionalOnProperty(name = "aegis.messaging.provider", havingValue = "pubsub")
public class PubSubSpecificationEventPublisher extends SpecificationEventPublisherBase {

    private static final Logger log = LoggerFactory.getLogger(PubSubSpecificationEventPublisher.class);

    private final PubSubTemplate pubSubTemplate;
    private final PubSubMessagingProperties properties;

    public PubSubSpecificationEventPublisher(PubSubTemplate pubSubTemplate, PubSubMessagingProperties properties) {
        this.pubSubTemplate = pubSubTemplate;
        this.properties = properties;
    }

    @Override
    public void publishSpecificationCreated(SpecificationCreatedEvent event) {
        String topic = properties.getSpecificationCreatedTopic();
        log.info("Publishing SpecificationCreatedEvent to Pub/Sub topic '{}' for specification ID: {}",
                topic, event.specificationId());

        pubSubTemplate.publish(topic, event);

        log.debug("Successfully published event to Pub/Sub topic '{}'", topic);
    }
}
