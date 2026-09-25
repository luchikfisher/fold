/**
 * Event semantics shared across FOLD.
 *
 * <p>Domain events describe facts inside a bounded context. Integration events
 * are explicit, versioned contracts that communicate facts across bounded
 * contexts.</p>
 *
 * <p>This package contains no transport implementation. Message brokers,
 * transactional outboxes, retries, routing, serialization, and delivery
 * mechanisms belong to the platform layer.</p>
 */
package com.fold.kernel.events;