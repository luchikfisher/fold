package com.fold.modules.observations.domain.event;

import com.fold.kernel.events.DomainEvent;
import com.fold.kernel.ids.ObservationId;
import com.fold.kernel.time.Timestamp;

import java.util.Objects;

/**
 * Records that an observation was accepted into downstream knowledge
 * processing.
 *
 * <p>This event describes an internal observations-domain fact. It is not the
 * versioned integration contract that will later be published to other bounded
 * contexts.</p>
 *
 * @param observationId the accepted observation
 * @param occurredAt    when the acceptance decision occurred
 */
public record ObservationAccepted(
        ObservationId observationId,
        Timestamp occurredAt
) implements DomainEvent {

    public ObservationAccepted {
        Objects.requireNonNull(
                observationId,
                "observationId must not be null"
        );
        Objects.requireNonNull(
                occurredAt,
                "occurredAt must not be null"
        );
    }
}