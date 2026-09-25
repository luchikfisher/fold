/**
 * Strongly typed identifiers shared across FOLD.
 *
 * <p>Each identifier represents exactly one domain concept. Distinct concepts
 * use distinct Java types even when they share the same physical
 * representation. This prevents identifiers belonging to unrelated concepts
 * from being accidentally interchanged.</p>
 *
 * <p>Identifiers contain identity only. Lifecycle and business behavior remain
 * the responsibility of the bounded context that owns the identified object.</p>
 */
package com.fold.kernel.ids;