package com.fold.modules.observations.application.feature.submitobservation;

import com.fold.kernel.ids.ObservationId;
import com.fold.modules.observations.domain.value.ObservationFingerprint;
import com.fold.modules.observations.domain.value.ObservationRejection;

import java.util.Objects;

/**
 * Represents the final outcome of submitting one observation candidate.
 */
public sealed interface SubmitObservationResult
        permits SubmitObservationResult.Accepted,
        SubmitObservationResult.Rejected,
        SubmitObservationResult.Duplicate {

    /**
     * The submission created and accepted a new observation.
     *
     * @param observationId the new observation
     * @param fingerprint   the observation fingerprint
     */
    record Accepted(
            ObservationId observationId,
            ObservationFingerprint fingerprint
    ) implements SubmitObservationResult {

        public Accepted {
            Objects.requireNonNull(
                    observationId,
                    "observationId must not be null"
            );
            Objects.requireNonNull(
                    fingerprint,
                    "fingerprint must not be null"
            );
        }
    }

    /**
     * The submission created a new observation but that observation was
     * rejected from downstream knowledge processing.
     *
     * @param observationId the retained observation
     * @param fingerprint   the observation fingerprint
     * @param rejection     the rejection decision
     */
    record Rejected(
            ObservationId observationId,
            ObservationFingerprint fingerprint,
            ObservationRejection rejection
    ) implements SubmitObservationResult {

        public Rejected {
            Objects.requireNonNull(
                    observationId,
                    "observationId must not be null"
            );
            Objects.requireNonNull(
                    fingerprint,
                    "fingerprint must not be null"
            );
            Objects.requireNonNull(
                    rejection,
                    "rejection must not be null"
            );
        }
    }

    /**
     * The submission was resolved to an existing observation.
     *
     * <p>No new observation was persisted.</p>
     *
     * @param existingObservationId the authoritative existing observation
     * @param fingerprint           the matching fingerprint
     */
    record Duplicate(
            ObservationId existingObservationId,
            ObservationFingerprint fingerprint
    ) implements SubmitObservationResult {

        public Duplicate {
            Objects.requireNonNull(
                    existingObservationId,
                    "existingObservationId must not be null"
            );
            Objects.requireNonNull(
                    fingerprint,
                    "fingerprint must not be null"
            );
        }
    }
}