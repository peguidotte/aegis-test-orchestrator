package com.aegis.tests.orchestrator.specification.exception;

import com.aegis.tests.orchestrator.shared.exception.InternalException;
import com.aegis.tests.orchestrator.specification.enums.SpecificationStatus;
import com.aegis.tests.orchestrator.specification.enums.SpecificationStatusTransitions;
import lombok.Getter;

import java.util.Set;

/**
 * Exception thrown when an invalid specification status transition is attempted.
 *
 * <p>This exception indicates a <strong>bug in the system logic</strong>, not a user error.
 * It should result in a 500 Internal Server Error response, as valid business flows
 * should never trigger invalid state transitions.</p>
 *
 * <p>Extends {@link InternalException} as this represents an internal system error,
 * not a business rule violation.</p>
 *
 * @see SpecificationStatus
 * @see SpecificationStatusTransitions
 */
@Getter
public class InvalidStatusTransitionException extends InternalException {

    private static final String ERROR_CODE = "INVALID_STATUS_TRANSITION";

    private final SpecificationStatus fromStatus;
    private final SpecificationStatus toStatus;
    private final Set<SpecificationStatus> allowedTransitions;

    /**
     * Creates a new InvalidStatusTransitionException.
     *
     * @param fromStatus Current status that the transition was attempted from
     * @param toStatus   Target status that was attempted
     */
    public InvalidStatusTransitionException(SpecificationStatus fromStatus, SpecificationStatus toStatus) {
        super(buildMessage(fromStatus, toStatus), ERROR_CODE);
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.allowedTransitions = SpecificationStatusTransitions.getAllowedTransitionsFrom(fromStatus);
    }

    private static String buildMessage(SpecificationStatus from, SpecificationStatus to) {
        Set<SpecificationStatus> allowed = SpecificationStatusTransitions.getAllowedTransitionsFrom(from);
        return String.format(
                "Invalid specification status transition: %s -> %s. Allowed transitions from %s: %s",
                from, to, from, allowed
        );
    }
}
