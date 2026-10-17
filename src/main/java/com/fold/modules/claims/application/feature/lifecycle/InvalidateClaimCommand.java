package com.fold.modules.claims.application.feature.lifecycle;

import com.fold.kernel.events.EventMetadata;
import com.fold.kernel.ids.ClaimId;
import com.fold.modules.claims.domain.value.ClaimLifecycleReason;
import com.fold.modules.claims.domain.value.ClaimRevision;

import java.util.Objects;

/**
 * Requests an explicit invalidation decision against one known Claim revision.
 * The optional cause supplies tracing context only, never retry identity.
 * A reason is required for this decision.
 */
public record InvalidateClaimCommand(
        ClaimId claimId,
        ClaimRevision expectedRevision,
        ClaimLifecycleReason reason,
        EventMetadata cause
) {
    public InvalidateClaimCommand {
        Objects.requireNonNull(claimId, "claimId must not be null");
        Objects.requireNonNull(expectedRevision, "expectedRevision must not be null");
        Objects.requireNonNull(reason, "reason must not be null");
    }
}
