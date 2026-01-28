package com.aegis.tests.orchestrator.specification.enums;

import com.aegis.tests.orchestrator.specification.exception.InvalidStatusTransitionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Set;
import java.util.stream.Stream;

import static com.aegis.tests.orchestrator.specification.enums.SpecificationStatus.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link SpecificationStatusTransitions}.
 */
@DisplayName("SpecificationStatusTransitions")
class SpecificationStatusTransitionsTest {

    @Nested
    @DisplayName("isValidTransition")
    class IsValidTransition {

        @ParameterizedTest(name = "{0} -> {1} should be valid")
        @MethodSource("com.aegis.tests.orchestrator.specification.enums.SpecificationStatusTransitionsTest#validTransitions")
        @DisplayName("should return true for valid transitions")
        void shouldReturnTrueForValidTransitions(SpecificationStatus from, SpecificationStatus to) {
            assertTrue(SpecificationStatusTransitions.isValidTransition(from, to));
        }

        @ParameterizedTest(name = "{0} -> {1} should be invalid")
        @MethodSource("com.aegis.tests.orchestrator.specification.enums.SpecificationStatusTransitionsTest#invalidTransitions")
        @DisplayName("should return false for invalid transitions")
        void shouldReturnFalseForInvalidTransitions(SpecificationStatus from, SpecificationStatus to) {
            assertFalse(SpecificationStatusTransitions.isValidTransition(from, to));
        }

        @Test
        @DisplayName("should return false when from is null")
        void shouldReturnFalseWhenFromIsNull() {
            assertFalse(SpecificationStatusTransitions.isValidTransition(null, PROCESSING));
        }

        @Test
        @DisplayName("should return false when to is null")
        void shouldReturnFalseWhenToIsNull() {
            assertFalse(SpecificationStatusTransitions.isValidTransition(CREATED, null));
        }

        @Test
        @DisplayName("should return false when both are null")
        void shouldReturnFalseWhenBothAreNull() {
            assertFalse(SpecificationStatusTransitions.isValidTransition(null, null));
        }
    }

    @Nested
    @DisplayName("getAllowedTransitionsFrom")
    class GetAllowedTransitionsFrom {

        @Test
        @DisplayName("should return valid targets from CREATED")
        void shouldReturnValidTargetsFromCreated() {
            Set<SpecificationStatus> allowed = SpecificationStatusTransitions.getAllowedTransitionsFrom(CREATED);

            assertEquals(Set.of(PROCESSING, ERROR), allowed);
        }

        @Test
        @DisplayName("should return valid targets from WAITING_APPROVAL")
        void shouldReturnValidTargetsFromWaitingApproval() {
            Set<SpecificationStatus> allowed = SpecificationStatusTransitions.getAllowedTransitionsFrom(WAITING_APPROVAL);

            assertEquals(Set.of(APPROVED, APPROVED_WITH_EDITS, REJECTED, ERROR), allowed);
        }

        @Test
        @DisplayName("should return valid targets from VALIDATING_TESTS including loop back")
        void shouldReturnValidTargetsFromValidatingTests() {
            Set<SpecificationStatus> allowed = SpecificationStatusTransitions.getAllowedTransitionsFrom(VALIDATING_TESTS);

            assertTrue(allowed.contains(TESTS_GENERATED));
            assertTrue(allowed.contains(GENERATING_TESTS)); // Loop back if validation fails
            assertTrue(allowed.contains(ERROR));
        }

        @Test
        @DisplayName("should return empty set for null status")
        void shouldReturnEmptySetForNullStatus() {
            Set<SpecificationStatus> allowed = SpecificationStatusTransitions.getAllowedTransitionsFrom(null);

            assertTrue(allowed.isEmpty());
        }
    }

    @Nested
    @DisplayName("validateTransition")
    class ValidateTransition {

        @Test
        @DisplayName("should not throw for valid transition")
        void shouldNotThrowForValidTransition() {
            assertDoesNotThrow(() ->
                SpecificationStatusTransitions.validateTransition(CREATED, PROCESSING)
            );
        }

        @Test
        @DisplayName("should throw InvalidStatusTransitionException for invalid transition")
        void shouldThrowInvalidStatusTransitionExceptionForInvalidTransition() {
            InvalidStatusTransitionException exception = assertThrows(
                InvalidStatusTransitionException.class,
                () -> SpecificationStatusTransitions.validateTransition(CREATED, TESTS_GENERATED)
            );

            assertEquals(CREATED, exception.getFromStatus());
            assertEquals(TESTS_GENERATED, exception.getToStatus());
            assertTrue(exception.getMessage().contains("Invalid specification status transition"));
            assertTrue(exception.getMessage().contains("CREATED"));
            assertTrue(exception.getMessage().contains("TESTS_GENERATED"));
        }

        @Test
        @DisplayName("should include allowed transitions in exception")
        void shouldIncludeAllowedTransitionsInException() {
            InvalidStatusTransitionException exception = assertThrows(
                InvalidStatusTransitionException.class,
                () -> SpecificationStatusTransitions.validateTransition(CREATED, APPROVED)
            );

            Set<SpecificationStatus> allowed = exception.getAllowedTransitions();
            assertTrue(allowed.contains(PROCESSING));
            assertTrue(allowed.contains(ERROR));
        }
    }

    @Nested
    @DisplayName("Complete Flow Scenarios")
    class CompleteFlowScenarios {

        @Test
        @DisplayName("happy path: CREATED -> PROCESSING -> PLANNING -> PLANNED -> WAITING_APPROVAL -> APPROVED -> PROCESSING -> GENERATING_TESTS -> VALIDATING_TESTS -> TESTS_GENERATED")
        void happyPathWithApproval() {
            // This test validates the complete happy path through the state machine
            assertTrue(SpecificationStatusTransitions.isValidTransition(CREATED, PROCESSING));
            assertTrue(SpecificationStatusTransitions.isValidTransition(PROCESSING, PLANNING));
            assertTrue(SpecificationStatusTransitions.isValidTransition(PLANNING, PLANNED));
            assertTrue(SpecificationStatusTransitions.isValidTransition(PLANNED, WAITING_APPROVAL));
            assertTrue(SpecificationStatusTransitions.isValidTransition(WAITING_APPROVAL, APPROVED));
            assertTrue(SpecificationStatusTransitions.isValidTransition(APPROVED, PROCESSING));
            assertTrue(SpecificationStatusTransitions.isValidTransition(PROCESSING, GENERATING_TESTS));
            assertTrue(SpecificationStatusTransitions.isValidTransition(GENERATING_TESTS, VALIDATING_TESTS));
            assertTrue(SpecificationStatusTransitions.isValidTransition(VALIDATING_TESTS, TESTS_GENERATED));
        }

        @Test
        @DisplayName("path with user edits: WAITING_APPROVAL -> APPROVED_WITH_EDITS -> PROCESSING")
        void pathWithUserEdits() {
            assertTrue(SpecificationStatusTransitions.isValidTransition(WAITING_APPROVAL, APPROVED_WITH_EDITS));
            assertTrue(SpecificationStatusTransitions.isValidTransition(APPROVED_WITH_EDITS, PROCESSING));
        }

        @Test
        @DisplayName("rejection path: WAITING_APPROVAL -> REJECTED -> PROCESSING")
        void rejectionPath() {
            assertTrue(SpecificationStatusTransitions.isValidTransition(WAITING_APPROVAL, REJECTED));
            assertTrue(SpecificationStatusTransitions.isValidTransition(REJECTED, PROCESSING));
        }

        @Test
        @DisplayName("validation retry loop: VALIDATING_TESTS -> GENERATING_TESTS -> VALIDATING_TESTS")
        void validationRetryLoop() {
            assertTrue(SpecificationStatusTransitions.isValidTransition(VALIDATING_TESTS, GENERATING_TESTS));
            assertTrue(SpecificationStatusTransitions.isValidTransition(GENERATING_TESTS, VALIDATING_TESTS));
        }

        @Test
        @DisplayName("error recovery: ERROR -> PROCESSING")
        void errorRecovery() {
            assertTrue(SpecificationStatusTransitions.isValidTransition(ERROR, PROCESSING));
        }

        @Test
        @DisplayName("any state can transition to ERROR")
        void anyStateCanTransitionToError() {
            for (SpecificationStatus status : SpecificationStatus.values()) {
                if (status != ERROR) {
                    assertTrue(
                        SpecificationStatusTransitions.isValidTransition(status, ERROR),
                        status + " should be able to transition to ERROR"
                    );
                }
            }
        }
    }

    /**
     * Provides valid transition pairs for parameterized tests.
     */
    static Stream<Arguments> validTransitions() {
        return Stream.of(
            // CREATED transitions
            Arguments.of(CREATED, PROCESSING),
            Arguments.of(CREATED, ERROR),

            // PROCESSING transitions
            Arguments.of(PROCESSING, PLANNING),
            Arguments.of(PROCESSING, GENERATING_TESTS),
            Arguments.of(PROCESSING, ERROR),

            // PLANNING transitions
            Arguments.of(PLANNING, PLANNED),
            Arguments.of(PLANNING, ERROR),

            // PLANNED transitions
            Arguments.of(PLANNED, WAITING_APPROVAL),
            Arguments.of(PLANNED, ERROR),

            // WAITING_APPROVAL transitions
            Arguments.of(WAITING_APPROVAL, APPROVED),
            Arguments.of(WAITING_APPROVAL, APPROVED_WITH_EDITS),
            Arguments.of(WAITING_APPROVAL, REJECTED),
            Arguments.of(WAITING_APPROVAL, ERROR),

            // APPROVED transitions
            Arguments.of(APPROVED, PROCESSING),
            Arguments.of(APPROVED, ERROR),

            // APPROVED_WITH_EDITS transitions
            Arguments.of(APPROVED_WITH_EDITS, PROCESSING),
            Arguments.of(APPROVED_WITH_EDITS, ERROR),

            // REJECTED transitions
            Arguments.of(REJECTED, PROCESSING),
            Arguments.of(REJECTED, ERROR),

            // GENERATING_TESTS transitions
            Arguments.of(GENERATING_TESTS, VALIDATING_TESTS),
            Arguments.of(GENERATING_TESTS, ERROR),

            // VALIDATING_TESTS transitions
            Arguments.of(VALIDATING_TESTS, TESTS_GENERATED),
            Arguments.of(VALIDATING_TESTS, GENERATING_TESTS),
            Arguments.of(VALIDATING_TESTS, ERROR),

            // TESTS_GENERATED transitions
            Arguments.of(TESTS_GENERATED, ERROR),

            // ERROR transitions
            Arguments.of(ERROR, PROCESSING)
        );
    }

    /**
     * Provides invalid transition pairs for parameterized tests.
     */
    static Stream<Arguments> invalidTransitions() {
        return Stream.of(
            // Cannot skip states
            Arguments.of(CREATED, PLANNING),
            Arguments.of(CREATED, TESTS_GENERATED),
            Arguments.of(CREATED, APPROVED),

            // Cannot go backwards (except specific loops)
            Arguments.of(PLANNING, CREATED),
            Arguments.of(TESTS_GENERATED, CREATED),
            Arguments.of(APPROVED, CREATED),

            // Cannot transition from terminal state (except ERROR -> PROCESSING)
            Arguments.of(TESTS_GENERATED, PROCESSING),
            Arguments.of(TESTS_GENERATED, GENERATING_TESTS),

            // Invalid jumps
            Arguments.of(WAITING_APPROVAL, GENERATING_TESTS),
            Arguments.of(PLANNING, TESTS_GENERATED),
            Arguments.of(APPROVED, TESTS_GENERATED)
        );
    }
}
