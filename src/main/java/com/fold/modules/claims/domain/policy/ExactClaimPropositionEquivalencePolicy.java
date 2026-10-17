package com.fold.modules.claims.domain.policy;

import com.fold.modules.claims.domain.value.ClaimProposition;

import java.util.Objects;

/**
 * Compares Claim propositions using exact Claims-domain value semantics.
 *
 * <p>Two propositions are exactly equivalent when their subject, predicate,
 * and object are equal according to the value objects owned by Claims.</p>
 *
 * <p>This policy deliberately performs no entity resolution, alias expansion,
 * ontology reasoning, unit conversion, temporal reasoning, fuzzy matching, or
 * other higher-level interpretation.</p>
 *
 * <p>For example, two unresolved references that later resolve to the same
 * canonical Entity are not considered equivalent by this policy unless their
 * current Claims-domain references are already equal.</p>
 *
 * <p>This narrow definition is intentional. Exact structural equivalence is a
 * stable primitive on top of which richer proposition-equivalence strategies
 * may later be constructed.</p>
 */
public final class ExactClaimPropositionEquivalencePolicy
        implements ClaimPropositionEquivalencePolicy {

    @Override
    public boolean equivalent(
            ClaimProposition left,
            ClaimProposition right
    ) {
        Objects.requireNonNull(
                left,
                "left must not be null"
        );

        Objects.requireNonNull(
                right,
                "right must not be null"
        );

        return left.equals(right);
    }
}