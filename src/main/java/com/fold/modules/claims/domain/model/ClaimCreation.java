package com.fold.modules.claims.domain.model;

import com.fold.modules.claims.domain.event.ClaimCreated;

import java.util.Objects;

/**
 * Result of creating a new Claim.
 *
 * <p>Creation returns both the authoritative aggregate and the corresponding
 * domain event so callers cannot accidentally separate initial state creation
 * from its semantic domain event.</p>
 *
 * @param claim created Claim aggregate
 * @param event corresponding creation event
 */
public record ClaimCreation(
        Claim claim,
        ClaimCreated event
) {

    public ClaimCreation {
        Objects.requireNonNull(
                claim,
                "claim must not be null"
        );

        Objects.requireNonNull(
                event,
                "event must not be null"
        );

        if (!claim.id().equals(event.claimId())) {
            throw new IllegalArgumentException(
                    "Claim and creation event must reference the same ClaimId"
            );
        }

        if (!claim.revision().equals(event.revision())) {
            throw new IllegalArgumentException(
                    "Claim and creation event must have the same revision"
            );
        }
    }
}