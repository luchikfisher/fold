/**
 * Semantic Claim derivation vocabulary.
 *
 * <p>Derivation rules transform normalized semantic input into zero or more
 * Claim proposals. They do not create authoritative Claims, assign ClaimIds,
 * persist state, publish integration events, or perform identity resolution.</p>
 *
 * <p>All Claim production must be explicit. There is deliberately no generic
 * rule that converts every upstream field into a Claim.</p>
 */
package com.fold.modules.claims.domain.derivation;