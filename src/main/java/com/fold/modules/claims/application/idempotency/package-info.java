/**
 * Application-level processing identity and idempotency vocabulary for Claims.
 *
 * <p>Processing idempotency is deliberately separate from both Claim identity
 * and proposition equivalence.</p>
 *
 * <pre>
 * ClaimId
 *     = identity of one assertion occurrence
 *
 * proposition equivalence
 *     = whether two assertions express the same proposition
 *
 * ClaimProcessingKey
 *     = whether one upstream derivation operation has already been processed
 * </pre>
 *
 * <p>Event-delivery identifiers, correlation identifiers, and causation
 * identifiers are tracing metadata and must not be substituted for semantic
 * processing identity.</p>
 */
package com.fold.modules.claims.application.idempotency;