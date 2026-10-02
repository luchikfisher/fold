package com.fold.modules.claims.domain.event;

import com.fold.kernel.events.DomainEvent;
import com.fold.kernel.ids.ClaimId;
import com.fold.kernel.time.Timestamp;
import com.fold.modules.claims.domain.value.ClaimRevision;

import java.util.Objects;

/**
 * Domain event emitted when a new Claim is created.
 */
public record ClaimCreated(
        ClaimId claimId,
        ClaimRevision revision,
        Timestamp occurredAt
) implements DomainEvent {

    public ClaimCreated {
        Objects.requireNonNull(
                claimId,
                "claimId must not be null"
        );

        Objects.requireNonNull(
                revision,
                "revision must not be null"
        );

        Objects.requireNonNull(
                occurredAt,
                "occurredAt must not be null"
        );

        if (!revision.equals(ClaimRevision.initial())) {
            throw new IllegalArgumentException(
                    "created Claim must use initial revision"
            );
        }
    }
}