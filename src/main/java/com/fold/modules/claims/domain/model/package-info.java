/**
 * Aggregate models owned by the Claims bounded context.
 *
 * <p>A Claim represents one assertion occurrence. Its proposition and creation
 * context are immutable. Only explicitly modeled lifecycle state may evolve,
 * and every lifecycle transition is preserved in authoritative revision
 * history.</p>
 */
package com.fold.modules.claims.domain.model;