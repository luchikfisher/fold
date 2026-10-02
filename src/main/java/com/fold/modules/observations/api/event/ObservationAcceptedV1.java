package com.fold.modules.observations.api.event;

import com.fold.kernel.events.EventVersion;
import com.fold.kernel.events.IntegrationEvent;
import com.fold.kernel.ids.ObservationId;
import com.fold.kernel.time.Timestamp;

import java.util.Objects;

/**
 * Version-one integration contract announcing that an observation has been
 * accepted into downstream knowledge processing.
 *
 * <p>Consumers should use the observation ID to reference or retrieve the
 * authoritative observation. This event intentionally does not reproduce the
 * entire observations-domain aggregate.</p>
 *
 * @param observationId the accepted observation
 * @param occurredAt    when the acceptance decision occurred
 */
public record ObservationAcceptedV1(
        ObservationId observationId,
        Timestamp occurredAt
) implements IntegrationEvent {

    public static final String TYPE =
            "observations.observation-accepted";

    public ObservationAcceptedV1 {
        Objects.requireNonNull(
                observationId,
                "observationId must not be null"
        );
        Objects.requireNonNull(
                occurredAt,
                "occurredAt must not be null"
        );
    }

    @Override
    public String eventType() {
        return TYPE;
    }

    @Override
    public EventVersion version() {
        return EventVersion.initial();
    }
}