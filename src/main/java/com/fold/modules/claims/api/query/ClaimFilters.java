package com.fold.modules.claims.api.query;

import com.fold.kernel.ids.ClaimId;
import com.fold.modules.claims.api.view.ClaimOriginReferenceView;
import com.fold.modules.claims.api.view.ClaimView;

import java.util.Set;

/**
 * Exact, conjunctive filters. Null means unrestricted, including all lifecycle
 * statuses. Origin matches a directly recorded reference; successorClaimId
 * selects predecessors explicitly superseded by that Claim, regardless of the
 * successor's current status. No equivalence or transitive ancestry is inferred.
 */
public record ClaimFilters(
        ClaimView.Subject subject,
        String predicate,
        String status,
        ClaimOriginReferenceView origin,
        ClaimId successorClaimId
) {
    public ClaimFilters {
        if (subject != null && (subject.namespace().isBlank() || subject.value().isBlank())) {
            throw new IllegalArgumentException("subject namespace and value must not be blank");
        }
        if (predicate != null && predicate.isBlank()) {
            throw new IllegalArgumentException("predicate must not be blank");
        }
        if (status != null && !Set.of("ACTIVE", "INVALIDATED", "SUPERSEDED").contains(status)) {
            throw new IllegalArgumentException("Unsupported Claim status");
        }
        if (origin != null && !Set.of("OBSERVATION", "SOURCE", "EVIDENCE", "CLAIM").contains(origin.kind())) {
            throw new IllegalArgumentException("Unsupported Claim origin kind");
        }
    }

    public static ClaimFilters all() {
        return new ClaimFilters(null, null, null, null, null);
    }
}
