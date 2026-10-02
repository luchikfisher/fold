package com.fold.modules.observations.domain.error;

import com.fold.kernel.ids.ObservationId;
import com.fold.kernel.result.DomainError;
import com.fold.modules.observations.domain.value.ObservationStatus;

import java.util.Objects;

/**
 * Describes an attempted observation lifecycle transition that is not allowed
 * from the observation's current state.
 *
 * <p>An observation may transition from {@link ObservationStatus#RECEIVED}
 * exactly once, either to {@link ObservationStatus#ACCEPTED} or to
 * {@link ObservationStatus#REJECTED}. Both terminal states are immutable.</p>
 *
 * @param observationId the observation whose transition was attempted
 * @param currentStatus the current lifecycle state
 * @param targetStatus  the requested lifecycle state
 */
public record ObservationTransitionError(
        ObservationId observationId,
        ObservationStatus currentStatus,
        ObservationStatus targetStatus
) implements DomainError {

    public ObservationTransitionError {
        Objects.requireNonNull(
                observationId,
                "observationId must not be null"
        );
        Objects.requireNonNull(
                currentStatus,
                "currentStatus must not be null"
        );
        Objects.requireNonNull(
                targetStatus,
                "targetStatus must not be null"
        );
    }

    @Override
    public String code() {
        return "observation.invalid-transition";
    }

    @Override
    public String message() {
        return "Observation %s cannot transition from %s to %s"
                .formatted(
                        observationId,
                        currentStatus,
                        targetStatus
                );
    }
}