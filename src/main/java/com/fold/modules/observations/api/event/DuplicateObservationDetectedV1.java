package com.fold.modules.observations.api.event;

import com.fold.kernel.events.EventVersion;
import com.fold.kernel.events.IntegrationEvent;
import com.fold.kernel.ids.ObservationId;
import com.fold.kernel.time.Timestamp;

import java.util.Objects;

/**
 * Version-one integration contract announcing that a submitted observation was
 * resolved to an already existing observation instead of creating a new one.
 *
 * <p>This event describes processing behavior rather than an observation
 * lifecycle transition. No new observation aggregate is created.</p>
 *
 * @param existingObservationId the observation that already represents the input
 * @param occurredAt            when the duplicate was detected
 */
public record DuplicateObservationDetectedV1(
        ObservationId existingObservationId,
        Timestamp occurredAt
) implements IntegrationEvent {

    public static final String TYPE =
            "observations.duplicate-observation-detected";

    public DuplicateObservationDetectedV1 {
        Objects.requireNonNull(
                existingObservationId,
                "existingObservationId must not be null"
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