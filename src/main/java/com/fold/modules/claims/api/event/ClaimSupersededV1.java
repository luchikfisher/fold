package com.fold.modules.claims.api.event;

import com.fold.kernel.events.EventVersion;
import com.fold.kernel.events.IntegrationEvent;
import com.fold.kernel.ids.ClaimId;
import com.fold.kernel.time.Timestamp;

import java.util.Objects;

/**
 * Announces a committed superseded lifecycle revision. Revision identifies
 * Claim-local order; occurredAt may equal another revision's timestamp.
 * The successor is historical lineage; its current state may change later. Reason is optional.
 */
public record ClaimSupersededV1(
        ClaimId claimId,
        ClaimId successorClaimId,
        long revision,
        ClaimLifecycleReasonV1 reason,
        Timestamp occurredAt
) implements IntegrationEvent {

    public static final String TYPE = "claims.claim-superseded";

    public ClaimSupersededV1 {
        Objects.requireNonNull(claimId, "claimId must not be null");
        Objects.requireNonNull(occurredAt, "occurredAt must not be null");
        if (revision < 2) {
            throw new IllegalArgumentException("lifecycle event revision must be greater than one");
        }
        Objects.requireNonNull(successorClaimId, "successorClaimId must not be null");
        if (claimId.equals(successorClaimId)) {
            throw new IllegalArgumentException("Claim cannot supersede itself");
        }
    }

    @Override
    public String eventType() {
        return TYPE;
    }

    @Override
    public EventVersion version() {
        return EventVersion.initial();
    }
}
