package com.fold.modules.observations.domain.policy;

import com.fold.modules.observations.domain.model.ObservationOrigin;
import com.fold.modules.observations.domain.model.ObservationPayload;
import com.fold.modules.observations.domain.model.ObservationSubject;
import com.fold.modules.observations.domain.value.ObservationFingerprint;
import com.fold.modules.observations.domain.value.ObservationType;
import com.fold.modules.observations.domain.value.ObservedAt;

import java.util.Objects;

/**
 * Produces a deterministic fingerprint for the semantic content of an
 * observation.
 *
 * <p>Fingerprinting exists exclusively for duplicate detection. It must not be
 * used as the aggregate identity of an observation.</p>
 *
 * <p>Implementations must be deterministic: semantically equivalent input
 * governed by the same fingerprint version must always produce the same
 * fingerprint.</p>
 */
public interface ObservationFingerprintPolicy {

    /**
     * Calculates the fingerprint of one normalized observation candidate.
     *
     * @param input semantic input to fingerprint
     * @return the deterministic fingerprint
     */
    ObservationFingerprint calculate(Input input);

    /**
     * Contains exactly the observation properties that participate in
     * duplicate identity.
     *
     * <p>Arrival time is intentionally absent because receiving the same
     * semantic observation at two different times must not produce two
     * fingerprints.</p>
     *
     * <p>Evidence identity is present inside {@link ObservationOrigin} but is
     * deliberately ignored by the version-one fingerprint implementation.
     * Evidence describes provenance of the submission, not the semantic
     * observation itself.</p>
     *
     * @param type       semantic observation type
     * @param subject    source-facing semantic subject
     * @param payload    normalized semantic payload
     * @param origin     provenance origin
     * @param observedAt source-facing observation time
     */
    record Input(
            ObservationType type,
            ObservationSubject subject,
            ObservationPayload payload,
            ObservationOrigin origin,
            ObservedAt observedAt
    ) {

        public Input {
            Objects.requireNonNull(
                    type,
                    "type must not be null"
            );
            Objects.requireNonNull(
                    subject,
                    "subject must not be null"
            );
            Objects.requireNonNull(
                    payload,
                    "payload must not be null"
            );
            Objects.requireNonNull(
                    origin,
                    "origin must not be null"
            );
            Objects.requireNonNull(
                    observedAt,
                    "observedAt must not be null"
            );
        }

        public static Input of(
                ObservationType type,
                ObservationSubject subject,
                ObservationPayload payload,
                ObservationOrigin origin,
                ObservedAt observedAt
        ) {
            return new Input(
                    type,
                    subject,
                    payload,
                    origin,
                    observedAt
            );
        }
    }
}