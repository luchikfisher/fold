package com.fold.modules.claims.infrastructure.persistence.record;

import java.time.Instant;
import java.util.UUID;

/**
 * Persistence representation of one Claim lifecycle revision.
 */
public record ClaimRevisionRecord(
        UUID claimId,
        long revision,
        String transition,
        String status,
        Instant changedAt,
        UUID relatedClaimId,
        String reasonCode,
        String reasonMessage
) {
}