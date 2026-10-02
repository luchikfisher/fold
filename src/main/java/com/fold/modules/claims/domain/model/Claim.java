package com.fold.modules.claims.domain.model;

import com.fold.kernel.ids.ClaimId;
import com.fold.kernel.time.Timestamp;
import com.fold.kernel.time.ValidTime;
import com.fold.modules.claims.domain.event.ClaimCreated;
import com.fold.modules.claims.domain.event.ClaimInvalidated;
import com.fold.modules.claims.domain.event.ClaimReinstated;
import com.fold.modules.claims.domain.event.ClaimSuperseded;
import com.fold.modules.claims.domain.value.ClaimConfidence;
import com.fold.modules.claims.domain.value.ClaimDerivation;
import com.fold.modules.claims.domain.value.ClaimLifecycleReason;
import com.fold.modules.claims.domain.value.ClaimLifecycleRevision;
import com.fold.modules.claims.domain.value.ClaimLifecycleTransition;
import com.fold.modules.claims.domain.value.ClaimObservedAt;
import com.fold.modules.claims.domain.value.ClaimOrigin;
import com.fold.modules.claims.domain.value.ClaimProposition;
import com.fold.modules.claims.domain.value.ClaimRevision;
import com.fold.modules.claims.domain.value.ClaimStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Aggregate root representing one individually identifiable assertion
 * occurrence in FOLD.
 *
 * <p>A Claim is not the canonical identity of a proposition. Multiple Claims
 * may contain semantically equal propositions while remaining distinct
 * assertions because they arose from different provenance, derivation,
 * evidence, observations, or processing occurrences.</p>
 *
 * <p>The proposition and its creation context are immutable. Lifecycle state
 * may evolve only through explicit domain transitions, each of which creates
 * a new authoritative {@link ClaimLifecycleRevision}.</p>
 *
 * <p>In particular, lifecycle operations must never mutate the subject,
 * predicate, or object of an existing Claim. A corrected or replacement
 * proposition must be represented by another Claim.</p>
 */
public final class Claim {

    private final ClaimId id;
    private final ClaimProposition proposition;
    private final ClaimOrigin origin;
    private final ClaimDerivation derivation;
    private final ClaimConfidence confidence;
    private final ClaimObservedAt observedAt;
    private final ValidTime validTime;
    private final Timestamp createdAt;

    private ClaimStatus status;
    private ClaimRevision revision;

    private final List<ClaimLifecycleRevision> revisionHistory;

    private Claim(
            ClaimId id,
            ClaimProposition proposition,
            ClaimOrigin origin,
            ClaimDerivation derivation,
            ClaimConfidence confidence,
            ClaimObservedAt observedAt,
            ValidTime validTime,
            Timestamp createdAt,
            List<ClaimLifecycleRevision> revisionHistory
    ) {
        this.id = Objects.requireNonNull(
                id,
                "id must not be null"
        );

        this.proposition = Objects.requireNonNull(
                proposition,
                "proposition must not be null"
        );

        this.origin = Objects.requireNonNull(
                origin,
                "origin must not be null"
        );

        this.derivation = Objects.requireNonNull(
                derivation,
                "derivation must not be null"
        );

        this.confidence = confidence;
        this.observedAt = observedAt;
        this.validTime = validTime;

        this.createdAt = Objects.requireNonNull(
                createdAt,
                "createdAt must not be null"
        );

        Objects.requireNonNull(
                revisionHistory,
                "revisionHistory must not be null"
        );

        validateRevisionHistory(
                id,
                createdAt,
                revisionHistory
        );

        this.revisionHistory =
                new ArrayList<>(revisionHistory);

        ClaimLifecycleRevision current =
                this.revisionHistory.getLast();

        this.status = current.status();
        this.revision = current.revision();
    }

    /**
     * Creates a new active Claim using a caller-supplied authoritative ID.
     *
     * <p>No relationship is inferred between observed time, valid time, and
     * creation time. They represent different temporal semantics.</p>
     */
    public static ClaimCreation create(
            ClaimId id,
            ClaimProposition proposition,
            ClaimOrigin origin,
            ClaimDerivation derivation,
            ClaimConfidence confidence,
            ClaimObservedAt observedAt,
            ValidTime validTime,
            Timestamp createdAt
    ) {
        ClaimLifecycleRevision initialRevision =
                ClaimLifecycleRevision.created(createdAt);

        Claim claim =
                new Claim(
                        id,
                        proposition,
                        origin,
                        derivation,
                        confidence,
                        observedAt,
                        validTime,
                        createdAt,
                        List.of(initialRevision)
                );

        ClaimCreated event =
                new ClaimCreated(
                        id,
                        ClaimRevision.initial(),
                        createdAt
                );

        return new ClaimCreation(
                claim,
                event
        );
    }

    /**
     * Convenience factory creating a new FOLD Claim identity.
     */
    public static ClaimCreation createNew(
            ClaimProposition proposition,
            ClaimOrigin origin,
            ClaimDerivation derivation,
            ClaimConfidence confidence,
            ClaimObservedAt observedAt,
            ValidTime validTime,
            Timestamp createdAt
    ) {
        return create(
                ClaimId.random(),
                proposition,
                origin,
                derivation,
                confidence,
                observedAt,
                validTime,
                createdAt
        );
    }

    /**
     * Restores an authoritative Claim from durable state.
     *
     * <p>Restoration performs no lifecycle transition and emits no event.
     * Persisted history must satisfy the same invariants as history created by
     * normal domain behavior.</p>
     */
    public static Claim restore(
            ClaimId id,
            ClaimProposition proposition,
            ClaimOrigin origin,
            ClaimDerivation derivation,
            ClaimConfidence confidence,
            ClaimObservedAt observedAt,
            ValidTime validTime,
            Timestamp createdAt,
            List<ClaimLifecycleRevision> revisionHistory
    ) {
        return new Claim(
                id,
                proposition,
                origin,
                derivation,
                confidence,
                observedAt,
                validTime,
                createdAt,
                revisionHistory
        );
    }

    /**
     * Supersedes this Claim with another Claim.
     *
     * <p>Supersession is terminal in Claims v1. A superseded Claim preserves
     * its proposition and complete historical state but can no longer return
     * to ACTIVE lifecycle status.</p>
     */
    public ClaimSuperseded supersedeBy(
            ClaimId successorClaimId,
            ClaimLifecycleReason reason,
            Timestamp changedAt
    ) {
        Objects.requireNonNull(
                successorClaimId,
                "successorClaimId must not be null"
        );

        Objects.requireNonNull(
                changedAt,
                "changedAt must not be null"
        );

        requireStatus(
                ClaimStatus.ACTIVE,
                "Only an ACTIVE Claim can be superseded"
        );

        if (id.equals(successorClaimId)) {
            throw new IllegalArgumentException(
                    "Claim cannot supersede itself"
            );
        }

        requireNonDecreasingTime(changedAt);

        ClaimRevision nextRevision =
                revision.next();

        ClaimLifecycleRevision lifecycleRevision =
                ClaimLifecycleRevision.superseded(
                        nextRevision,
                        changedAt,
                        successorClaimId,
                        reason
                );

        appendRevision(lifecycleRevision);

        return new ClaimSuperseded(
                id,
                successorClaimId,
                nextRevision,
                reason,
                changedAt
        );
    }

    /**
     * Explicitly invalidates this active Claim.
     */
    public ClaimInvalidated invalidate(
            ClaimLifecycleReason reason,
            Timestamp changedAt
    ) {
        Objects.requireNonNull(
                reason,
                "reason must not be null"
        );

        Objects.requireNonNull(
                changedAt,
                "changedAt must not be null"
        );

        requireStatus(
                ClaimStatus.ACTIVE,
                "Only an ACTIVE Claim can be invalidated"
        );

        requireNonDecreasingTime(changedAt);

        ClaimRevision nextRevision =
                revision.next();

        ClaimLifecycleRevision lifecycleRevision =
                ClaimLifecycleRevision.invalidated(
                        nextRevision,
                        changedAt,
                        reason
                );

        appendRevision(lifecycleRevision);

        return new ClaimInvalidated(
                id,
                nextRevision,
                reason,
                changedAt
        );
    }

    /**
     * Reinstates a previously invalidated Claim.
     *
     * <p>Reinstatement does not erase the invalidation. Both transitions remain
     * present in the authoritative revision history.</p>
     */
    public ClaimReinstated reinstate(
            ClaimLifecycleReason reason,
            Timestamp changedAt
    ) {
        Objects.requireNonNull(
                reason,
                "reason must not be null"
        );

        Objects.requireNonNull(
                changedAt,
                "changedAt must not be null"
        );

        requireStatus(
                ClaimStatus.INVALIDATED,
                "Only an INVALIDATED Claim can be reinstated"
        );

        requireNonDecreasingTime(changedAt);

        ClaimRevision nextRevision =
                revision.next();

        ClaimLifecycleRevision lifecycleRevision =
                ClaimLifecycleRevision.reinstated(
                        nextRevision,
                        changedAt,
                        reason
                );

        appendRevision(lifecycleRevision);

        return new ClaimReinstated(
                id,
                nextRevision,
                reason,
                changedAt
        );
    }

    private void appendRevision(
            ClaimLifecycleRevision lifecycleRevision
    ) {
        revisionHistory.add(lifecycleRevision);

        status = lifecycleRevision.status();
        revision = lifecycleRevision.revision();
    }

    private void requireStatus(
            ClaimStatus expected,
            String message
    ) {
        if (status != expected) {
            throw new IllegalStateException(
                    message
                            + "; current status is "
                            + status
            );
        }
    }

    private void requireNonDecreasingTime(
            Timestamp changedAt
    ) {
        Timestamp latest =
                revisionHistory
                        .getLast()
                        .changedAt();

        if (changedAt.isBefore(latest)) {
            throw new IllegalArgumentException(
                    "lifecycle transition time must not precede "
                            + "the latest Claim revision"
            );
        }
    }

    private static void validateRevisionHistory(
            ClaimId claimId,
            Timestamp createdAt,
            List<ClaimLifecycleRevision> history
    ) {
        if (history.isEmpty()) {
            throw new IllegalArgumentException(
                    "revisionHistory must not be empty"
            );
        }

        if (history.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException(
                    "revisionHistory must not contain null"
            );
        }

        ClaimLifecycleRevision first =
                history.getFirst();

        if (first.transition()
                != ClaimLifecycleTransition.CREATED) {
            throw new IllegalArgumentException(
                    "first Claim revision must be CREATED"
            );
        }

        if (!first.revision()
                .equals(ClaimRevision.initial())) {
            throw new IllegalArgumentException(
                    "first Claim revision must be revision 1"
            );
        }

        if (!first.changedAt().equals(createdAt)) {
            throw new IllegalArgumentException(
                    "createdAt must equal the CREATED revision time"
            );
        }

        ClaimLifecycleRevision previous = first;

        for (int index = 1; index < history.size(); index++) {
            ClaimLifecycleRevision current =
                    history.get(index);

            long expectedRevision =
                    Math.incrementExact(
                            previous.revision().value()
                    );

            if (current.revision().value()
                    != expectedRevision) {
                throw new IllegalArgumentException(
                        "Claim revisions must be contiguous"
                );
            }

            if (current.changedAt()
                    .isBefore(previous.changedAt())) {
                throw new IllegalArgumentException(
                        "Claim revision times must be non-decreasing"
                );
            }

            validateTransition(
                    claimId,
                    previous,
                    current
            );

            previous = current;
        }
    }

    private static void validateTransition(
            ClaimId claimId,
            ClaimLifecycleRevision previous,
            ClaimLifecycleRevision current
    ) {
        ClaimStatus previousStatus =
                previous.status();

        ClaimLifecycleTransition transition =
                current.transition();

        switch (previousStatus) {
            case ACTIVE -> {
                if (transition
                        != ClaimLifecycleTransition.SUPERSEDED
                        && transition
                        != ClaimLifecycleTransition.INVALIDATED) {
                    throw new IllegalArgumentException(
                            "ACTIVE Claim may transition only to "
                                    + "SUPERSEDED or INVALIDATED"
                    );
                }
            }

            case INVALIDATED -> {
                if (transition
                        != ClaimLifecycleTransition.REINSTATED) {
                    throw new IllegalArgumentException(
                            "INVALIDATED Claim may transition only "
                                    + "through REINSTATED"
                    );
                }
            }

            case SUPERSEDED ->
                    throw new IllegalArgumentException(
                            "SUPERSEDED Claim is terminal"
                    );
        }

        current.relatedClaimId().ifPresent(relatedClaimId -> {
            if (claimId.equals(relatedClaimId)) {
                throw new IllegalArgumentException(
                        "Claim cannot reference itself as successor"
                );
            }
        });
    }

    public ClaimId id() {
        return id;
    }

    public ClaimProposition proposition() {
        return proposition;
    }

    public ClaimOrigin origin() {
        return origin;
    }

    public ClaimDerivation derivation() {
        return derivation;
    }

    public Optional<ClaimConfidence> confidence() {
        return Optional.ofNullable(confidence);
    }

    public Optional<ClaimObservedAt> observedAt() {
        return Optional.ofNullable(observedAt);
    }

    public Optional<ValidTime> validTime() {
        return Optional.ofNullable(validTime);
    }

    public Timestamp createdAt() {
        return createdAt;
    }

    public ClaimStatus status() {
        return status;
    }

    public ClaimRevision revision() {
        return revision;
    }

    /**
     * Returns the complete authoritative lifecycle revision history.
     */
    public List<ClaimLifecycleRevision> revisionHistory() {
        return List.copyOf(revisionHistory);
    }

    public boolean isActive() {
        return status == ClaimStatus.ACTIVE;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof Claim that)) {
            return false;
        }

        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Claim[id="
                + id
                + ", status="
                + status
                + ", revision="
                + revision.value()
                + "]";
    }
}