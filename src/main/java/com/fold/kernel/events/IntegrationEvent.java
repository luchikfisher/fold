package com.fold.kernel.events;

import com.fold.kernel.time.Timestamp;

/**
 * Represents a versioned fact published across a bounded-context boundary.
 *
 * <p>An integration event communicates something that has already happened
 * without exposing the publishing module's internal domain model.</p>
 *
 * <p>The event type and version are part of a stable consumer contract.</p>
 */
public interface IntegrationEvent {

    /**
     * Returns the stable logical type of this event.
     *
     * <p>Examples include {@code claim-created} and
     * {@code entity-merged}.</p>
     *
     * @return the event type
     */
    String eventType();

    /**
     * Returns the version of the published contract.
     *
     * @return the event version
     */
    EventVersion version();

    /**
     * Returns when the represented domain fact occurred.
     *
     * @return the occurrence time
     */
    Timestamp occurredAt();
}