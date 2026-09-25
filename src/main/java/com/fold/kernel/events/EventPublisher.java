package com.fold.kernel.events;

/**
 * Publishes integration events beyond the boundary in which they were
 * produced.
 *
 * <p>This interface describes publication semantics only. Transport,
 * persistence, retry behavior, batching, broker acknowledgements, topics, and
 * partitions belong to infrastructure.</p>
 */
@FunctionalInterface
public interface EventPublisher {

    /**
     * Publishes an integration-event envelope.
     *
     * @param event the event to publish
     */
    void publish(EventEnvelope<? extends IntegrationEvent> event);
}