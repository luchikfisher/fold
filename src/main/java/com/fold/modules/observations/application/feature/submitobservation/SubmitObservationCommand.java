package com.fold.modules.observations.application.feature.submitobservation;

import com.fold.modules.observations.domain.model.ObservationOrigin;
import com.fold.modules.observations.domain.model.ObservationPayload;
import com.fold.modules.observations.domain.model.ObservationSubject;
import com.fold.modules.observations.domain.value.ObservationType;
import com.fold.modules.observations.domain.value.ObservedAt;

import java.util.Objects;

/**
 * Requests submission of one normalized semantic observation to FOLD.
 *
 * <p>The command contains source-facing semantic input only. Aggregate
 * identity, arrival time, fingerprint, and lifecycle state are assigned by the
 * submission use case.</p>
 *
 * @param type       semantic observation type
 * @param subject    source-facing semantic subject
 * @param payload    normalized semantic payload
 * @param origin     provenance origin
 * @param observedAt source-facing observation time
 */
public record SubmitObservationCommand(
        ObservationType type,
        ObservationSubject subject,
        ObservationPayload payload,
        ObservationOrigin origin,
        ObservedAt observedAt
) {

    public SubmitObservationCommand {
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
}