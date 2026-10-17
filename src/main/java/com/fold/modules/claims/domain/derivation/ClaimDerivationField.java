package com.fold.modules.claims.domain.derivation;

import java.util.Objects;

/**
 * Represents one named semantic field available during Claim derivation.
 *
 * @param name  semantic field name
 * @param value typed semantic value
 */
public record ClaimDerivationField(
        String name,
        ClaimDerivationValue value
) {

    public ClaimDerivationField {
        Objects.requireNonNull(
                name,
                "name must not be null"
        );

        if (name.isBlank()) {
            throw new IllegalArgumentException(
                    "name must not be blank"
            );
        }

        Objects.requireNonNull(
                value,
                "value must not be null"
        );
    }
}