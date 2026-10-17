package com.fold.modules.claims.api.event;

import com.fold.kernel.events.EventVersion;
import com.fold.kernel.events.IntegrationEvent;
import com.fold.kernel.ids.ClaimId;
import com.fold.kernel.time.Timestamp;

import java.util.Objects;

/**
 * Announces a committed invalidated lifecycle revision. Revision identifies
 * Claim-local order; occurredAt may equal another revision's timestamp.
 * Reason records the explicit decision, not an assessment of epistemic truth.
 */
public record ClaimInvalidatedV1(
        ClaimId claimId,
        long revision,
        ClaimLifecycleReasonV1 reason,
        Timestamp occurredAt
) implements IntegrationEvent {

    public static final String TYPE = "claims.claim-invalidated";

    public ClaimInvalidatedV1 {
        Objects.requireNonNull(claimId, "claimId must not be null");
        Objects.requireNonNull(occurredAt, "occurredAt must not be null");
        if (revision < 2) {
            throw new IllegalArgumentException("lifecycle event revision must be greater than one");
        }
        Objects.requireNonNull(reason, "reason must not be null");
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
