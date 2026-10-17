package com.fold.modules.claims.infrastructure.persistence.record;

import java.time.Instant;
import java.util.UUID;

/**
 * Database representation of one authoritative Claim row.
 */
public record ClaimRecord(
        UUID id,
        String subjectNamespace,
        String subjectValue,
        String predicate,
        String objectPayload,
        String derivationKind,
        String derivationMethod,
        int derivationVersion,
        Double confidence,
        Instant observedAt,
        Instant validFrom,
        Instant validTo,
        Instant createdAt,
        String status,
        long revision
) {
}