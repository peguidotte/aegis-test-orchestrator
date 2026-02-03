package com.aegis.tests.orchestrator.specification.messaging;

import com.aegis.tests.orchestrator.shared.config.PubSubMessagingProperties;
import com.aegis.tests.orchestrator.specification.dto.event.SpecificationCreatedEvent;
import com.aegis.tests.orchestrator.specification.enums.SpecificationInputType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PubSubSpecificationEventPublisher")
class PubSubSpecificationEventPublisherTest {

    @Mock
    private PubSubTemplate pubSubTemplate;

    private PubSubMessagingProperties properties;
    private ObjectMapper objectMapper;
    private PubSubSpecificationEventPublisher publisher;

    private static final String TOPIC = "aegis-test.test-generation.started";

    @BeforeEach
    void setUp() {
        properties = new PubSubMessagingProperties();
        properties.setTestGenerationRequestedTopic(TOPIC);
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        publisher = new PubSubSpecificationEventPublisher(pubSubTemplate, properties, objectMapper);
    }

    @Test
    @DisplayName("should publish SpecificationCreatedEvent to configured topic")
    void shouldPublishSpecificationCreatedEventToConfiguredTopic() throws Exception {
        // Arrange
        var event = createTestEvent(10L, "POST", "/api/v1/invoices");
        String payload = objectMapper.writeValueAsString(event);
        when(pubSubTemplate.publish(eq(TOPIC), eq(payload)))
                .thenReturn(CompletableFuture.completedFuture("message-id"));

        // Act
        publisher.publishSpecificationCreated(event);

        // Assert
        verify(pubSubTemplate).publish(TOPIC, payload);
    }

    @Test
    @DisplayName("should use topic from properties")
    void shouldUseTopicFromProperties() throws Exception {
        // Arrange
        String customTopic = "custom-topic";
        properties.setTestGenerationRequestedTopic(customTopic);

        var event = createTestEvent(1L, "GET", "/api/test");
        String payload = objectMapper.writeValueAsString(event);
        when(pubSubTemplate.publish(eq(customTopic), eq(payload)))
                .thenReturn(CompletableFuture.completedFuture("message-id"));

        // Act
        publisher.publishSpecificationCreated(event);

        // Assert
        verify(pubSubTemplate).publish(customTopic, payload);
    }

    private SpecificationCreatedEvent createTestEvent(Long specId, String method, String path) {
        return new SpecificationCreatedEvent(
                specId,
                "Test Specification",
                "Test Description",
                SpecificationInputType.MANUAL,
                method,
                path,
                "Test objective",
                "{\"test\": \"data\"}",
                false,
                false,
                null,
                null,
                null,
                null,
                null,
                List.of(),
                "trace-456",
                Instant.now()
        );
    }
}
