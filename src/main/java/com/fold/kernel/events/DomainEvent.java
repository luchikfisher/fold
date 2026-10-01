package com.fold.kernel.events;

import com.fold.kernel.time.Timestamp;

/**
 * Represents a fact that has already occurred inside a domain model.
 *
 * <p>A domain event belongs to the internal language of its bounded context.
 * It is not automatically a stable contract for consumers outside that
 * context.</p>
 */
public interface DomainEvent {

    /**
     * Returns when the represented domain fact occurred.
     *
     * @return the occurrence time
     */
    Timestamp occurredAt();
}