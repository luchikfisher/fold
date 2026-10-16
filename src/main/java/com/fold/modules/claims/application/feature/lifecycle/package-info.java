/**
 * Explicit Claim lifecycle operations with revision-bound idempotency.
 *
 * <p>The semantic operation is a transition from a specified Claim revision,
 * including its exact reason and successor. Identical requests against that
 * revision share an outcome. A retry returns the historical next revision,
 * even after later transitions; a different request against a consumed
 * revision conflicts. Callers must retain the original expected revision on
 * retry. Supplying the current revision instead requests a new decision.</p>
 *
 * <p>Lifecycle history is the durable receipt. Cause metadata and clock time
 * are not command identity. Replays emit no additional event and do not
 * revalidate a successor's current eligibility. No delivery-ID deduplication
 * or separate operation receipt store is implied by this contract.</p>
 */
package com.fold.modules.claims.application.feature.lifecycle;
