package com.fold.kernel.events;

import java.util.Objects;

/**
 * Couples an integration-event payload with metadata describing its emitted
 * message and causal context.
 *
 * <p>The payload answers "what happened". The metadata answers "which emission
 * is this, when was it emitted, by whom, and as a consequence of what".</p>
 *
 * @param metadata the emission metadata
 * @param payload  the integration-event payload
 * @param <T>      the concrete event type
 */
public record EventEnvelope<T extends IntegrationEvent>(
        EventMetadata metadata,
        T payload
) {

    public EventEnvelope {
        Objects.requireNonNull(metadata, "metadata must not be null");
        Objects.requireNonNull(payload, "payload must not be null");
    }

    public String eventType() {
        return payload.eventType();
    }

    public EventVersion version() {
        return payload.version();
    }
}