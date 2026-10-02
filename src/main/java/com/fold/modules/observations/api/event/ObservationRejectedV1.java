package com.fold.modules.observations.api.event;

import com.fold.kernel.events.EventVersion;
import com.fold.kernel.events.IntegrationEvent;
import com.fold.kernel.ids.ObservationId;
import com.fold.kernel.time.Timestamp;

import java.util.Objects;

/**
 * Version-one integration contract announcing that an observation was retained
 * but rejected from downstream knowledge processing.
 *
 * @param observationId    the rejected observation
 * @param rejectionCode    stable machine-readable rejection code
 * @param rejectionMessage human-readable rejection explanation
 * @param occurredAt       when the rejection decision occurred
 */
public record ObservationRejectedV1(
        ObservationId observationId,
        String rejectionCode,
        String rejectionMessage,
        Timestamp occurredAt
) implements IntegrationEvent {

    public static final String TYPE =
            "observations.observation-rejected";

    public ObservationRejectedV1 {
        Objects.requireNonNull(
                observationId,
                "observationId must not be null"
        );
        Objects.requireNonNull(
                rejectionCode,
                "rejectionCode must not be null"
        );
        Objects.requireNonNull(
                rejectionMessage,
                "rejectionMessage must not be null"
        );
        Objects.requireNonNull(
                occurredAt,
                "occurredAt must not be null"
        );

        if (rejectionCode.isBlank()) {
            throw new IllegalArgumentException(
                    "rejectionCode must not be blank"
            );
        }

        if (rejectionMessage.isBlank()) {
            throw new IllegalArgumentException(
                    "rejectionMessage must not be blank"
            );
        }
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