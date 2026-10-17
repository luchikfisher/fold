package com.fold.modules.claims.api.view;

import java.util.Objects;
import java.util.UUID;

/**
 * Public representation of one Claim provenance reference.
 */
public record ClaimOriginReferenceView(
        String kind,
        UUID referenceId
) {

    public ClaimOriginReferenceView {
        Objects.requireNonNull(
                kind,
                "kind must not be null"
        );

        Objects.requireNonNull(
                referenceId,
                "referenceId must not be null"
        );
    }
}