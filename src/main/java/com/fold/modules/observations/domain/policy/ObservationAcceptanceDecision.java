package com.fold.modules.observations.domain.policy;

import com.fold.modules.observations.domain.value.ObservationRejection;

import java.util.Objects;

/**
 * Represents the outcome of evaluating whether a received observation may
 * enter downstream knowledge processing.
 *
 * <p>Acceptance means that the observation is admissible according to the
 * current observations-domain rules. Rejection retains the observation but
 * prevents it from entering downstream knowledge processing.</p>
 */
public sealed interface ObservationAcceptanceDecision
        permits ObservationAcceptanceDecision.Accepted,
        ObservationAcceptanceDecision.Rejected {

    /**
     * Indicates whether the observation is accepted.
     *
     * @return {@code true} for an accepted decision
     */
    boolean isAccepted();

    /**
     * Indicates whether the observation is rejected.
     *
     * @return {@code true} for a rejected decision
     */
    default boolean isRejected() {
        return !isAccepted();
    }

    /**
     * Represents an acceptance decision.
     */
    record Accepted()
            implements ObservationAcceptanceDecision {

        @Override
        public boolean isAccepted() {
            return true;
        }
    }

    /**
     * Represents a rejection decision together with its reason.
     *
     * @param rejection the reason the observation is not admissible
     */
    record Rejected(
            ObservationRejection rejection
    ) implements ObservationAcceptanceDecision {

        public Rejected {
            Objects.requireNonNull(
                    rejection,
                    "rejection must not be null"
            );
        }

        @Override
        public boolean isAccepted() {
            return false;
        }
    }

    /**
     * Creates an acceptance decision.
     *
     * @return an accepted decision
     */
    static Accepted accepted() {
        return new Accepted();
    }

    /**
     * Creates a rejection decision.
     *
     * @param rejection the rejection reason
     * @return a rejected decision
     */
    static Rejected rejected(
            ObservationRejection rejection
    ) {
        return new Rejected(rejection);
    }
}