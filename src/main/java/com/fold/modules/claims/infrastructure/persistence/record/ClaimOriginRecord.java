package com.fold.modules.claims.infrastructure.persistence.record;

import java.util.UUID;

/**
 * Persistence representation of one Claim provenance reference.
 */
public record ClaimOriginRecord(
        UUID claimId,
        String kind,
        UUID referenceId
) {
}