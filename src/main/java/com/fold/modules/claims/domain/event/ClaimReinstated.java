package com.fold.modules.claims.domain.event;

import com.fold.kernel.events.DomainEvent;
import com.fold.kernel.ids.ClaimId;
import com.fold.kernel.time.Timestamp;
import com.fold.modules.claims.domain.value.ClaimLifecycleReason;
import com.fold.modules.claims.domain.value.ClaimRevision;

import java.util.Objects;

/**
 * Domain event emitted when a previously invalidated Claim is explicitly
 * returned to active lifecycle state.
 */
public record ClaimReinstated(
        ClaimId claimId,
        ClaimRevision revision,
        ClaimLifecycleReason reason,
        Timestamp occurredAt
) implements DomainEvent {

    public ClaimReinstated {
        Objects.requireNonNull(
                claimId,
                "claimId must not be null"
        );

        Objects.requireNonNull(
                revision,
                "revision must not be null"
        );

        Objects.requireNonNull(
                reason,
                "reason must not be null"
        );

        Objects.requireNonNull(
                occurredAt,
                "occurredAt must not be null"
        );
    }
}