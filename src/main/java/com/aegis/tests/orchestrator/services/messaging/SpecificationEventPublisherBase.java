package com.aegis.tests.orchestrator.services.messaging;

import com.aegis.tests.orchestrator.model.dto.SpecificationCreatedEvent;

public abstract class SpecificationEventPublisherBase {

    /**
     * Publishes a SpecificationCreatedEvent.
     *
     * @param event the specification created event
     */
    public abstract void publishSpecificationCreated(SpecificationCreatedEvent event);
}

