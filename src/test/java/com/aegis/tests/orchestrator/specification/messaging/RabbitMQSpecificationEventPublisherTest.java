package com.aegis.tests.orchestrator.specification.messaging;

import com.aegis.tests.orchestrator.shared.config.RabbitMQConfig;
import com.aegis.tests.orchestrator.specification.dto.event.SpecificationCreatedEvent;
import com.aegis.tests.orchestrator.specification.enums.SpecificationInputType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("RabbitMQSpecificationEventPublisher")
class RabbitMQSpecificationEventPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    private RabbitMQSpecificationEventPublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new RabbitMQSpecificationEventPublisher(rabbitTemplate);
    }

    @Test
    @DisplayName("should publish SpecificationCreatedEvent to configured exchange with routing key")
    void shouldPublishSpecificationCreatedEventToExchange() {
        // Arrange
        var event = createTestEvent(10L, "POST", "/api/v1/invoices");

        // Act
        publisher.publishSpecificationCreated(event);

        // Assert
        verify(rabbitTemplate).convertAndSend(
                RabbitMQConfig.TEST_GENERATION_EXCHANGE,
                RabbitMQConfig.TEST_GENERATION_ROUTING_KEY,
                event
        );
    }

    @Test
    @DisplayName("should publish event with correct exchange and routing key")
    void shouldPublishWithCorrectExchangeAndRoutingKey() {
        // Arrange
        var event = createTestEvent(1L, "GET", "/api/test");

        // Act
        publisher.publishSpecificationCreated(event);

        // Assert
        verify(rabbitTemplate).convertAndSend(
                "aegis-test.test-generation.exchange",
                "specification.started",
                event
        );
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
                Instant.now()
        );
    }
}
