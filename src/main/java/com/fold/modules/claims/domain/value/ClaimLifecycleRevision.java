package com.fold.modules.claims.domain.value;

import com.fold.kernel.ids.ClaimId;
import com.fold.kernel.time.Timestamp;

import java.util.Objects;
import java.util.Optional;

/**
 * Represents one authoritative revision in the lifecycle history of a Claim.
 *
 * <p>A lifecycle revision records a state transition without permitting
 * mutation of the Claim proposition itself.</p>
 *
 * <p>The relationship between transition and resulting status is fixed:</p>
 *
 * <pre>
 * CREATED     -> ACTIVE
 * SUPERSEDED  -> SUPERSEDED
 * INVALIDATED -> INVALIDATED
 * REINSTATED  -> ACTIVE
 * </pre>
 *
 * <p>Supersession additionally identifies the replacing Claim. Invalidation
 * and reinstatement require an explicit reason.</p>
 */
public final class ClaimLifecycleRevision {

    private final ClaimRevision revision;
    private final ClaimLifecycleTransition transition;
    private final ClaimStatus status;
    private final Timestamp changedAt;
    private final ClaimId relatedClaimId;
    private final ClaimLifecycleReason reason;

    private ClaimLifecycleRevision(
            ClaimRevision revision,
            ClaimLifecycleTransition transition,
            ClaimStatus status,
            Timestamp changedAt,
            ClaimId relatedClaimId,
            ClaimLifecycleReason reason
    ) {
        this.revision = Objects.requireNonNull(
                revision,
                "revision must not be null"
        );

        this.transition = Objects.requireNonNull(
                transition,
                "transition must not be null"
        );

        this.status = Objects.requireNonNull(
                status,
                "status must not be null"
        );

        this.changedAt = Objects.requireNonNull(
                changedAt,
                "changedAt must not be null"
        );

        validateSemantics(
                revision,
                transition,
                status,
                relatedClaimId,
                reason
        );

        this.relatedClaimId = relatedClaimId;
        this.reason = reason;
    }

    public static ClaimLifecycleRevision created(
            Timestamp changedAt
    ) {
        return new ClaimLifecycleRevision(
                ClaimRevision.initial(),
                ClaimLifecycleTransition.CREATED,
                ClaimStatus.ACTIVE,
                changedAt,
                null,
                null
        );
    }

    public static ClaimLifecycleRevision superseded(
            ClaimRevision revision,
            Timestamp changedAt,
            ClaimId successorClaimId,
            ClaimLifecycleReason reason
    ) {
        Objects.requireNonNull(
                successorClaimId,
                "successorClaimId must not be null"
        );

        return new ClaimLifecycleRevision(
                revision,
                ClaimLifecycleTransition.SUPERSEDED,
                ClaimStatus.SUPERSEDED,
                changedAt,
                successorClaimId,
                reason
        );
    }

    public static ClaimLifecycleRevision invalidated(
            ClaimRevision revision,
            Timestamp changedAt,
            ClaimLifecycleReason reason
    ) {
        Objects.requireNonNull(
                reason,
                "reason must not be null"
        );

        return new ClaimLifecycleRevision(
                revision,
                ClaimLifecycleTransition.INVALIDATED,
                ClaimStatus.INVALIDATED,
                changedAt,
                null,
                reason
        );
    }

    public static ClaimLifecycleRevision reinstated(
            ClaimRevision revision,
            Timestamp changedAt,
            ClaimLifecycleReason reason
    ) {
        Objects.requireNonNull(
                reason,
                "reason must not be null"
        );

        return new ClaimLifecycleRevision(
                revision,
                ClaimLifecycleTransition.REINSTATED,
                ClaimStatus.ACTIVE,
                changedAt,
                null,
                reason
        );
    }

    /**
     * Reconstructs one lifecycle revision from authoritative persistence.
     *
     * <p>The same semantic invariants used by normal factories remain active.
     * Persistence therefore cannot reconstruct an impossible lifecycle
     * revision.</p>
     */
    public static ClaimLifecycleRevision restore(
            ClaimRevision revision,
            ClaimLifecycleTransition transition,
            ClaimStatus status,
            Timestamp changedAt,
            ClaimId relatedClaimId,
            ClaimLifecycleReason reason
    ) {
        return new ClaimLifecycleRevision(
                revision,
                transition,
                status,
                changedAt,
                relatedClaimId,
                reason
        );
    }

    private static void validateSemantics(
            ClaimRevision revision,
            ClaimLifecycleTransition transition,
            ClaimStatus status,
            ClaimId relatedClaimId,
            ClaimLifecycleReason reason
    ) {
        switch (transition) {
            case CREATED -> {
                if (!revision.equals(ClaimRevision.initial())) {
                    throw new IllegalArgumentException(
                            "CREATED transition must use initial revision"
                    );
                }

                if (status != ClaimStatus.ACTIVE) {
                    throw new IllegalArgumentException(
                            "CREATED transition must result in ACTIVE status"
                    );
                }

                if (relatedClaimId != null) {
                    throw new IllegalArgumentException(
                            "CREATED transition must not reference another Claim"
                    );
                }

                if (reason != null) {
                    throw new IllegalArgumentException(
                            "CREATED transition must not have a lifecycle reason"
                    );
                }
            }

            case SUPERSEDED -> {
                if (status != ClaimStatus.SUPERSEDED) {
                    throw new IllegalArgumentException(
                            "SUPERSEDED transition must result in SUPERSEDED status"
                    );
                }

                if (relatedClaimId == null) {
                    throw new IllegalArgumentException(
                            "SUPERSEDED transition requires successor Claim"
                    );
                }
            }

            case INVALIDATED -> {
                if (status != ClaimStatus.INVALIDATED) {
                    throw new IllegalArgumentException(
                            "INVALIDATED transition must result in INVALIDATED status"
                    );
                }

                if (relatedClaimId != null) {
                    throw new IllegalArgumentException(
                            "INVALIDATED transition must not reference another Claim"
                    );
                }

                if (reason == null) {
                    throw new IllegalArgumentException(
                            "INVALIDATED transition requires a reason"
                    );
                }
            }

            case REINSTATED -> {
                if (status != ClaimStatus.ACTIVE) {
                    throw new IllegalArgumentException(
                            "REINSTATED transition must result in ACTIVE status"
                    );
                }

                if (relatedClaimId != null) {
                    throw new IllegalArgumentException(
                            "REINSTATED transition must not reference another Claim"
                    );
                }

                if (reason == null) {
                    throw new IllegalArgumentException(
                            "REINSTATED transition requires a reason"
                    );
                }
            }
        }
    }

    public ClaimRevision revision() {
        return revision;
    }

    public ClaimLifecycleTransition transition() {
        return transition;
    }

    public ClaimStatus status() {
        return status;
    }

    public Timestamp changedAt() {
        return changedAt;
    }

    public Optional<ClaimId> relatedClaimId() {
        return Optional.ofNullable(relatedClaimId);
    }

    public Optional<ClaimLifecycleReason> reason() {
        return Optional.ofNullable(reason);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof ClaimLifecycleRevision that)) {
            return false;
        }

        return revision.equals(that.revision)
                && transition == that.transition
                && status == that.status
                && changedAt.equals(that.changedAt)
                && Objects.equals(
                relatedClaimId,
                that.relatedClaimId
        )
                && Objects.equals(reason, that.reason);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                revision,
                transition,
                status,
                changedAt,
                relatedClaimId,
                reason
        );
    }
}