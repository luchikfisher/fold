package com.fold.modules.observations.domain.event;

import com.fold.kernel.events.DomainEvent;
import com.fold.kernel.ids.ObservationId;
import com.fold.kernel.time.Timestamp;
import com.fold.modules.observations.domain.value.ObservationRejection;

import java.util.Objects;

/**
 * Records that an observation was rejected from downstream knowledge
 * processing.
 *
 * <p>Rejection does not remove the observation. The observation remains part
 * of FOLD's authoritative ingestion history together with the reason for the
 * decision.</p>
 *
 * @param observationId the rejected observation
 * @param rejection     the reason for rejection
 * @param occurredAt    when the rejection decision occurred
 */
public record ObservationRejected(
        ObservationId observationId,
        ObservationRejection rejection,
        Timestamp occurredAt
) implements DomainEvent {

    public ObservationRejected {
        Objects.requireNonNull(
                observationId,
                "observationId must not be null"
        );
        Objects.requireNonNull(
                rejection,
                "rejection must not be null"
        );
        Objects.requireNonNull(
                occurredAt,
                "occurredAt must not be null"
        );
    }
}