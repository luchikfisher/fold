package com.fold.modules.observations.infrastructure.persistence.record;

import java.time.Instant;
import java.util.UUID;

/**
 * Database representation of one observation row.
 *
 * <p>This type belongs exclusively to persistence infrastructure and must not
 * escape into the domain or application layers.</p>
 */
public record ObservationRecord(
        UUID id,
        String type,
        String subjectType,
        String subjectExternalKey,
        UUID sourceId,
        UUID evidenceId,
        String externalRecordId,
        int fingerprintVersion,
        String fingerprintAlgorithm,
        String fingerprintValue,
        Instant observedAt,
        Instant arrivedAt,
        String status,
        Instant decidedAt,
        String rejectionCode,
        String rejectionMessage,
        String payload
) {
}