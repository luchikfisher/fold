/**
 * Universal temporal primitives used by FOLD.
 *
 * <p>The package distinguishes absolute time from temporal meaning. A
 * {@link com.fold.kernel.time.Timestamp} identifies a point on the timeline,
 * while {@link com.fold.kernel.time.ValidTime} and
 * {@link com.fold.kernel.time.KnowledgeTime} represent different temporal
 * dimensions of the knowledge model.</p>
 *
 * <p>Domain-specific temporal concepts remain in their owning bounded
 * contexts.</p>
 */
package com.fold.kernel.time;