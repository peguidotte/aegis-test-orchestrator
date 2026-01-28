package com.aegis.tests.orchestrator.specification.enums;

import com.aegis.tests.orchestrator.specification.exception.InvalidStatusTransitionException;

import java.util.Map;
import java.util.Set;

/**
 * Defines valid state transitions for {@link SpecificationStatus}.
 *
 * <p>This class enforces the specification lifecycle by explicitly declaring
 * which transitions are allowed from each status. Invalid transitions indicate
 * a bug in the system logic and should be treated as internal errors.</p>
 *
 * <h3>Design Rationale:</h3>
 * <ul>
 *   <li>Prevents invalid state transitions at runtime</li>
 *   <li>Centralizes transition logic for maintainability</li>
 *   <li>Makes the state machine explicit and documented</li>
 *   <li>Enables early detection of logic bugs</li>
 * </ul>
 */
public final class SpecificationStatusTransitions {

    private SpecificationStatusTransitions() {
        // Utility class - prevent instantiation
    }

    /**
     * Map of valid transitions: currentStatus -> Set of allowed next statuses.
     */
    private static final Map<SpecificationStatus, Set<SpecificationStatus>> VALID_TRANSITIONS = Map.ofEntries(

            // Initial state can move to processing or error
            Map.entry(SpecificationStatus.CREATED, Set.of(
                    SpecificationStatus.PROCESSING,
                    SpecificationStatus.ERROR
            )),

            // Processing can move to planning or error
            Map.entry(SpecificationStatus.PROCESSING, Set.of(
                    SpecificationStatus.PLANNING,
                    SpecificationStatus.GENERATING_TESTS, // After approval, goes back to processing then generating
                    SpecificationStatus.ERROR
            )),

            // Planning can move to planned or error
            Map.entry(SpecificationStatus.PLANNING, Set.of(
                    SpecificationStatus.PLANNED,
                    SpecificationStatus.ERROR
            )),

            // Planned can move to waiting approval or error
            Map.entry(SpecificationStatus.PLANNED, Set.of(
                    SpecificationStatus.WAITING_APPROVAL,
                    SpecificationStatus.ERROR
            )),

            // Waiting approval can be approved, approved with edits, rejected, or error
            Map.entry(SpecificationStatus.WAITING_APPROVAL, Set.of(
                    SpecificationStatus.APPROVED,
                    SpecificationStatus.APPROVED_WITH_EDITS,
                    SpecificationStatus.REJECTED,
                    SpecificationStatus.ERROR
            )),

            // Approved moves to processing for test generation
            Map.entry(SpecificationStatus.APPROVED, Set.of(
                    SpecificationStatus.PROCESSING,
                    SpecificationStatus.ERROR
            )),

            // Approved with edits also moves to processing for test generation
            Map.entry(SpecificationStatus.APPROVED_WITH_EDITS, Set.of(
                    SpecificationStatus.PROCESSING,
                    SpecificationStatus.ERROR
            )),

            // Rejected goes back to processing for replanning
            Map.entry(SpecificationStatus.REJECTED, Set.of(
                    SpecificationStatus.PROCESSING,
                    SpecificationStatus.ERROR
            )),

            // Generating tests can move to validating or error
            Map.entry(SpecificationStatus.GENERATING_TESTS, Set.of(
                    SpecificationStatus.VALIDATING_TESTS,
                    SpecificationStatus.ERROR
            )),

            // Validating can complete, loop back to generating, or error
            Map.entry(SpecificationStatus.VALIDATING_TESTS, Set.of(
                    SpecificationStatus.TESTS_GENERATED,
                    SpecificationStatus.GENERATING_TESTS, // Loop back if validation fails
                    SpecificationStatus.ERROR
            )),

            // Terminal success state - no transitions allowed
            Map.entry(SpecificationStatus.TESTS_GENERATED, Set.of(
                    SpecificationStatus.ERROR // Can still fail after generation
            )),

            // Terminal error state - can potentially be retried from the beginning
            Map.entry(SpecificationStatus.ERROR, Set.of(
                    SpecificationStatus.PROCESSING // Allow retry from error
            ))
    );

    /**
     * Checks if a transition from one status to another is valid.
     *
     * @param from Current status
     * @param to   Target status
     * @return true if the transition is allowed, false otherwise
     */
    public static boolean isValidTransition(SpecificationStatus from, SpecificationStatus to) {
        if (from == null || to == null) {
            return false;
        }
        Set<SpecificationStatus> allowedTargets = VALID_TRANSITIONS.get(from);
        return allowedTargets != null && allowedTargets.contains(to);
    }

    /**
     * Gets all valid target statuses from the given status.
     *
     * @param from Current status
     * @return Set of valid target statuses, or empty set if none
     */
    public static Set<SpecificationStatus> getAllowedTransitionsFrom(SpecificationStatus from) {
        if (from == null) {
            return Set.of();
        }
        return VALID_TRANSITIONS.getOrDefault(from, Set.of());
    }

    /**
     * Validates a transition and throws an exception if invalid.
     *
     * <p>This method is intended to be used as a guard in business logic.
     * An invalid transition indicates a bug in the system, not a user error,
     * so it throws an {@link InvalidStatusTransitionException}.</p>
     *
     * @param from Current status
     * @param to   Target status
     * @throws InvalidStatusTransitionException if the transition is not valid
     */
    public static void validateTransition(SpecificationStatus from, SpecificationStatus to) {
        if (!isValidTransition(from, to)) {
            throw new InvalidStatusTransitionException(from, to);
        }
    }
}
