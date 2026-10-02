package com.fold.modules.claims.domain.event;

import com.fold.kernel.events.DomainEvent;
import com.fold.kernel.ids.ClaimId;
import com.fold.kernel.time.Timestamp;
import com.fold.modules.claims.domain.value.ClaimLifecycleReason;
import com.fold.modules.claims.domain.value.ClaimRevision;

import java.util.Objects;
import java.util.Optional;

/**
 * Domain event emitted when one Claim is explicitly superseded by another.
 */
public final class ClaimSuperseded implements DomainEvent {

    private final ClaimId claimId;
    private final ClaimId successorClaimId;
    private final ClaimRevision revision;
    private final ClaimLifecycleReason reason;
    private final Timestamp occurredAt;

    public ClaimSuperseded(
            ClaimId claimId,
            ClaimId successorClaimId,
            ClaimRevision revision,
            ClaimLifecycleReason reason,
            Timestamp occurredAt
    ) {
        this.claimId = Objects.requireNonNull(
                claimId,
                "claimId must not be null"
        );

        this.successorClaimId = Objects.requireNonNull(
                successorClaimId,
                "successorClaimId must not be null"
        );

        this.revision = Objects.requireNonNull(
                revision,
                "revision must not be null"
        );

        this.reason = reason;

        this.occurredAt = Objects.requireNonNull(
                occurredAt,
                "occurredAt must not be null"
        );

        if (claimId.equals(successorClaimId)) {
            throw new IllegalArgumentException(
                    "Claim cannot supersede itself"
            );
        }
    }

    public ClaimId claimId() {
        return claimId;
    }

    public ClaimId successorClaimId() {
        return successorClaimId;
    }

    public ClaimRevision revision() {
        return revision;
    }

    public Optional<ClaimLifecycleReason> reason() {
        return Optional.ofNullable(reason);
    }

    @Override
    public Timestamp occurredAt() {
        return occurredAt;
    }
}