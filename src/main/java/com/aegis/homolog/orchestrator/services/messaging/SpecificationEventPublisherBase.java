package com.aegis.homolog.orchestrator.services.messaging;

import com.aegis.homolog.orchestrator.model.dto.SpecificationCreatedEvent;

public abstract class SpecificationEventPublisherBase {

    /**
     * Publishes a SpecificationCreatedEvent.
     *
     * @param event the specification created event
     */
    public abstract void publishSpecificationCreated(SpecificationCreatedEvent event);
}

