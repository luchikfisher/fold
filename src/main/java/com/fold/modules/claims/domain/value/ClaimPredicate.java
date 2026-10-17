package com.fold.modules.claims.domain.value;

import java.util.Objects;

/**
 * Identifies the semantic relationship or property asserted by a claim.
 *
 * <p>Predicates form an open vocabulary. They are intentionally not modeled
 * as an enum because FOLD must be able to support new semantic predicates
 * without changing the compiled Claims domain model.</p>
 *
 * <p>This value object validates structural usability only. It does not
 * silently normalize, lowercase, trim, or otherwise reinterpret the supplied
 * predicate.</p>
 *
 * @param value predicate identifier
 */
public record ClaimPredicate(
        String value
) {

    public ClaimPredicate {
        Objects.requireNonNull(
                value,
                "value must not be null"
        );

        if (value.isBlank()) {
            throw new IllegalArgumentException(
                    "value must not be blank"
            );
        }
    }

    public static ClaimPredicate of(String value) {
        return new ClaimPredicate(value);
    }

    @Override
    public String toString() {
        return value;
    }
}