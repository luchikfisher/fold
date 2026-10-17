package com.fold.modules.claims.domain.derivation;

import java.util.Objects;

/**
 * Represents the unresolved subject supplied to a Claim derivation rule.
 *
 * <p>This value preserves upstream subject semantics without asserting that
 * the subject is a canonical FOLD Entity and without forcing the Claims
 * domain to depend on the Observations domain model.</p>
 *
 * @param type        upstream semantic subject type
 * @param externalKey upstream subject key
 */
public record ClaimDerivationSubject(
        String type,
        String externalKey
) {

    public ClaimDerivationSubject {
        type = requireNonBlank(type, "type");
        externalKey = requireNonBlank(
                externalKey,
                "externalKey"
        );
    }

    private static String requireNonBlank(
            String value,
            String name
    ) {
        Objects.requireNonNull(
                value,
                name + " must not be null"
        );

        if (value.isBlank()) {
            throw new IllegalArgumentException(
                    name + " must not be blank"
            );
        }

        return value;
    }
}