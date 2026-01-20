package com.aegis.homolog.orchestrator.model.enums;

/**
 * Defines the input modality for creating a Specification.
 * <p>
 * MANUAL: User provides method, path, and request example directly.
 * API_CALL: User references an existing ApiCall from the catalog.
 */
public enum SpecificationInputType {
    /**
     * User provides endpoint details manually (method, path, request example).
     */
    MANUAL,

    /**
     * User references an existing ApiCall from the endpoint catalog.
     * Method and path are resolved from the ApiCall.
     */
    API_CALL
}
