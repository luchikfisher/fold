package com.fold.modules.claims.domain.value;

import java.util.Objects;

/**
 * Represents the immutable semantic proposition asserted by a Claim.
 *
 * <p>A proposition consists of a subject, predicate, and object:</p>
 *
 * <pre>
 * subject --predicate--> object
 * </pre>
 *
 * <p>This type does not represent Claim identity. Multiple independently
 * created Claims may contain equal propositions while remaining distinct
 * Claims with different provenance, derivation, time, or lifecycle.</p>
 *
 * <p>Likewise, equality of two {@code ClaimProposition} instances does not by
 * itself establish that FOLD should merge, deduplicate, corroborate, or
 * otherwise treat the corresponding Claims as identical. Those are separate
 * semantic decisions.</p>
 *
 * @param subject   asserted subject
 * @param predicate asserted relationship or property
 * @param object    asserted object or value
 */
public record ClaimProposition(
        ClaimSubject subject,
        ClaimPredicate predicate,
        ClaimObject object
) {

    public ClaimProposition {
        Objects.requireNonNull(
                subject,
                "subject must not be null"
        );

        Objects.requireNonNull(
                predicate,
                "predicate must not be null"
        );

        Objects.requireNonNull(
                object,
                "object must not be null"
        );
    }
}