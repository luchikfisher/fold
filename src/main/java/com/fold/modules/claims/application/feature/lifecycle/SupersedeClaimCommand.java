package com.fold.modules.claims.application.feature.lifecycle;

import com.fold.kernel.events.EventMetadata;
import com.fold.kernel.ids.ClaimId;
import com.fold.modules.claims.domain.value.ClaimLifecycleReason;
import com.fold.modules.claims.domain.value.ClaimRevision;

import java.util.Objects;

/**
 * Requests an explicit supersession decision against one known Claim revision.
 * The optional cause supplies tracing context only, never retry identity.
 * A supersession reason is optional, matching the existing domain contract.
 */
public record SupersedeClaimCommand(
        ClaimId claimId,
        ClaimRevision expectedRevision,
        ClaimId successorClaimId,
        ClaimLifecycleReason reason,
        EventMetadata cause
) {
    public SupersedeClaimCommand {
        Objects.requireNonNull(claimId, "claimId must not be null");
        Objects.requireNonNull(expectedRevision, "expectedRevision must not be null");
        Objects.requireNonNull(successorClaimId, "successorClaimId must not be null");
        if (claimId.equals(successorClaimId)) {
            throw new IllegalArgumentException("Claim cannot supersede itself");
        }
    }
}
