package com.fold.modules.claims.domain.policy;

import com.fold.modules.claims.domain.value.ClaimProposition;

/**
 * Determines whether two Claim propositions are semantically equivalent under
 * a particular equivalence strategy.
 *
 * <p>Proposition equivalence is deliberately separate from Claim identity.
 * Two Claims may express equivalent propositions while remaining distinct
 * assertion occurrences with different {@code ClaimId} values, provenance,
 * derivation, temporal context, and lifecycle.</p>
 *
 * <p>An equivalence policy must compare proposition semantics only. It must not
 * use Claim identity, provenance, confidence, lifecycle state, or processing
 * metadata to decide whether the propositions are equivalent.</p>
 */
public interface ClaimPropositionEquivalencePolicy {

    /**
     * Determines whether the supplied propositions are equivalent according
     * to this policy.
     *
     * @param left  first proposition
     * @param right second proposition
     * @return {@code true} when the propositions are equivalent according to
     *         this policy
     */
    boolean equivalent(
            ClaimProposition left,
            ClaimProposition right
    );
}