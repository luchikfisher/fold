package com.fold.modules.claims.api.query;

import com.fold.kernel.ids.ClaimId;

import java.util.Objects;

/**
 * Requests one authoritative Claim by identity.
 */
public record GetClaimQuery(
        ClaimId claimId
) {

    public GetClaimQuery {
        Objects.requireNonNull(
                claimId,
                "claimId must not be null"
        );
    }
}