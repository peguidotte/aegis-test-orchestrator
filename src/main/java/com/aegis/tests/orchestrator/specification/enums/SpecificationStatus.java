package com.aegis.tests.orchestrator.specification.enums;

/**
 * Represents the lifecycle status of a Specification.
 *
 * <h3>Status Flow:</h3>
 * <pre>
 * CREATED ──────────────────────────────────────────────┐
 *    │                                                  │
 *    ▼                                                  │
 * PROCESSING ──► PLANNING ──► PLANNED ──► WAITING_APPROVAL
 *                                              │
 *                    ┌─────────────────────────┼─────────────────────────┐
 *                    │                         │                         │
 *                    ▼                         ▼                         ▼
 *              APPROVED             APPROVED_WITH_EDITS              REJECTED
 *                    │                         │                         │
 *                    └───────────┬─────────────┘                         │
 *                                │                                       │
 *                                ▼                                       │
 *                           PROCESSING ◄─────────────────────────────────┘
 *                                │
 *                                ▼
 *                        GENERATING_TESTS ◄───┐
 *                                │            │
 *                                ▼            │
 *                         VALIDATING_TESTS ───┘ (loop if tests fail validation)
 *                                │
 *                                ▼
 *                         TESTS_GENERATED
 *
 * Any status can transition to ERROR on failure.
 * </pre>
 */
public enum SpecificationStatus {

    /**
     * Initial status - Specification was just created and persisted.
     */
    CREATED,

    /**
     * AI agent received the specification and started processing.
     */
    PROCESSING,

    /**
     * AI agent is planning the test scenarios.
     */
    PLANNING,

    /**
     * AI agent finished planning - test scenarios are defined.
     */
    PLANNED,

    /**
     * Planning sent to user, waiting for approval or corrections.
     */
    WAITING_APPROVAL,

    /**
     * User approved the planning without changes.
     */
    APPROVED,

    /**
     * User approved the planning but made some edits.
     */
    APPROVED_WITH_EDITS,

    /**
     * User rejected the planning - needs replanning.
     */
    REJECTED,

    /**
     * AI generator received the approved planning and is generating test code.
     */
    GENERATING_TESTS,

    /**
     * AI generator is validating the generated tests (running them).
     */
    VALIDATING_TESTS,

    /**
     * Tests were successfully generated and validated.
     */
    TESTS_GENERATED,

    /**
     * An error occurred during any phase of processing.
     */
    ERROR
}

