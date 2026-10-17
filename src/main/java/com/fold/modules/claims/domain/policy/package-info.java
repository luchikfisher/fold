/**
 * Domain policies owned by the Claims bounded context.
 *
 * <p>Policies in this package answer semantic questions that do not belong to
 * Claim aggregate identity itself.</p>
 *
 * <p>In particular, proposition equivalence must remain separate from Claim
 * identity. Equal or equivalent propositions may belong to distinct Claims.</p>
 */
package com.fold.modules.claims.domain.policy;