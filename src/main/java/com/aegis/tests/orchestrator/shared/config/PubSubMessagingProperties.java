package com.aegis.tests.orchestrator.shared.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for GCP Pub/Sub messaging.
 */
@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "aegis.messaging.pubsub")
public class PubSubMessagingProperties {

    /**
     * The Pub/Sub topic for test generation requests.
     */
    private String testGenerationRequestedTopic = "aegis-test.test-generation.requested";
}
