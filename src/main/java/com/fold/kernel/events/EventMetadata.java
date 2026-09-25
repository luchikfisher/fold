package com.fold.kernel.events;

import com.fold.kernel.ids.CorrelationId;
import com.fold.kernel.ids.EventId;
import com.fold.kernel.time.Timestamp;

import java.util.Objects;
import java.util.Optional;

/**
 * Describes the identity, emission time, producer, and causal lineage of an
 * integration-event emission.
 *
 * <p>The metadata describes the emitted message. It does not form part of the
 * business fact contained in the event payload.</p>
 *
 * @param eventId the unique identity of this emission
 * @param correlationId the causal chain to which this event belongs
 * @param causationId the event that immediately caused this event, if any
 * @param emittedAt when this event was emitted
 * @param producer the logical component that emitted the event
 */
public record EventMetadata(
        EventId eventId,
        CorrelationId correlationId,
        EventId causationId,
        Timestamp emittedAt,
        String producer
) {

    public EventMetadata {
        Objects.requireNonNull(eventId, "eventId must not be null");
        Objects.requireNonNull(
                correlationId,
                "correlationId must not be null"
        );
        Objects.requireNonNull(
                emittedAt,
                "emittedAt must not be null"
        );
        Objects.requireNonNull(producer, "producer must not be null");

        if (producer.isBlank()) {
            throw new IllegalArgumentException(
                    "producer must not be blank"
            );
        }
    }

    /**
     * Creates metadata for the first event in a causal chain.
     *
     * @param emittedAt the emission time
     * @param producer the logical producer
     * @return metadata for a root event
     */
    public static EventMetadata root(
            Timestamp emittedAt,
            String producer
    ) {
        EventId eventId = EventId.random();

        return new EventMetadata(
                eventId,
                new CorrelationId(eventId.value()),
                null,
                emittedAt,
                producer
        );
    }

    /**
     * Creates metadata for an event caused by another emitted event.
     *
     * @param correlationId the inherited causal-chain identifier
     * @param causationId the immediately preceding event
     * @param emittedAt the emission time
     * @param producer the logical producer
     * @return metadata for the caused event
     */
    public static EventMetadata causedBy(
            CorrelationId correlationId,
            EventId causationId,
            Timestamp emittedAt,
            String producer
    ) {
        Objects.requireNonNull(
                causationId,
                "causationId must not be null"
        );

        return new EventMetadata(
                EventId.random(),
                correlationId,
                causationId,
                emittedAt,
                producer
        );
    }

    /**
     * Returns the event that immediately caused this event, when one exists.
     *
     * @return the causation event identifier
     */
    public Optional<EventId> causation() {
        return Optional.ofNullable(causationId);
    }

    /**
     * Indicates whether this event starts its causal chain.
     *
     * @return {@code true} when no causation event exists
     */
    public boolean isRoot() {
        return causationId == null;
    }
}