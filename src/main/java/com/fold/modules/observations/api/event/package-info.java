/**
 * Versioned integration-event contracts published by the observations bounded
 * context.
 *
 * <p>These contracts are part of the module's public API. They expose facts
 * required by consumers without exposing internal domain objects or
 * implementation details.</p>
 *
 * <p>Domain events and integration events are deliberately separate types.
 * Internal domain evolution must not silently change an already published
 * integration contract.</p>
 */
package com.fold.modules.observations.api.event;