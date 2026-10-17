package com.fold.modules.claims.domain.value;

import java.util.Objects;

/**
 * Describes how a claim was produced.
 *
 * <p>Derivation is independent of provenance. The origin identifies the
 * upstream artifacts involved; the derivation describes the semantic process
 * that transformed available information into the assertion.</p>
 *
 * <p>Neither the derivation kind nor method establishes that the resulting
 * proposition is true.</p>
 *
 * @param kind   broad derivation category
 * @param method concrete versioned derivation method
 */
public record ClaimDerivation(
        ClaimDerivationKind kind,
        ClaimDerivationMethod method
) {

    public ClaimDerivation {
        Objects.requireNonNull(
                kind,
                "kind must not be null"
        );

        Objects.requireNonNull(
                method,
                "method must not be null"
        );
    }
}