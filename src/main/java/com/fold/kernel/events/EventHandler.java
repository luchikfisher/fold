package com.fold.kernel.events;

/**
 * Handles one concrete integration-event contract.
 *
 * <p>Delivery semantics such as retries, ordering, concurrency, and
 * idempotency are supplied by infrastructure rather than by this interface.</p>
 *
 * @param <T> the handled integration-event type
 */
@FunctionalInterface
public interface EventHandler<T extends IntegrationEvent> {

    /**
     * Handles one delivered event.
     *
     * @param event the delivered event
     */
    void handle(EventEnvelope<T> event);
}