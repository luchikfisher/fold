package com.fold.modules.claims.domain.value;

import java.util.Objects;

/**
 * Identifies the versioned method responsible for producing a claim.
 *
 * <p>The method identifier allows FOLD to distinguish not merely that a claim
 * was, for example, derived, but which concrete extraction, derivation, or
 * inference procedure produced it.</p>
 *
 * <p>Versions are explicit because changing semantic derivation rules may
 * change the meaning of generated claims and must therefore remain
 * reproducible and historically traceable.</p>
 *
 * @param name    stable method identifier
 * @param version positive semantic method version
 */
public record ClaimDerivationMethod(
        String name,
        int version
) {

    public ClaimDerivationMethod {
        Objects.requireNonNull(
                name,
                "name must not be null"
        );

        if (name.isBlank()) {
            throw new IllegalArgumentException(
                    "name must not be blank"
            );
        }

        if (version < 1) {
            throw new IllegalArgumentException(
                    "version must be greater than zero"
            );
        }
    }

    public static ClaimDerivationMethod versionOne(
            String name
    ) {
        return new ClaimDerivationMethod(name, 1);
    }
}