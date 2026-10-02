package com.fold.modules.claims.domain.model;

import com.fold.kernel.ids.ClaimId;
import com.fold.kernel.time.Timestamp;
import com.fold.modules.claims.domain.event.ClaimInvalidated;
import com.fold.modules.claims.domain.event.ClaimReinstated;
import com.fold.modules.claims.domain.event.ClaimSuperseded;
import com.fold.modules.claims.domain.value.ClaimConfidence;
import com.fold.modules.claims.domain.value.ClaimDerivation;
import com.fold.modules.claims.domain.value.ClaimDerivationKind;
import com.fold.modules.claims.domain.value.ClaimDerivationMethod;
import com.fold.modules.claims.domain.value.ClaimLifecycleReason;
import com.fold.modules.claims.domain.value.ClaimLifecycleRevision;
import com.fold.modules.claims.domain.value.ClaimObject;
import com.fold.modules.claims.domain.value.ClaimOrigin;
import com.fold.modules.claims.domain.value.ClaimPredicate;
import com.fold.modules.claims.domain.value.ClaimProposition;
import com.fold.modules.claims.domain.value.ClaimReference;
import com.fold.modules.claims.domain.value.ClaimRevision;
import com.fold.modules.claims.domain.value.ClaimStatus;
import com.fold.modules.claims.domain.value.ClaimSubject;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClaimTest {

    private static final Timestamp T0 =
            Timestamp.parse(
                    "2026-10-02T10:00:00Z"
            );

    private static final Timestamp T1 =
            Timestamp.parse(
                    "2026-10-02T11:00:00Z"
            );

    private static final Timestamp T2 =
            Timestamp.parse(
                    "2026-10-02T12:00:00Z"
            );

    private static final Timestamp T3 =
            Timestamp.parse(
                    "2026-10-02T13:00:00Z"
            );

    private static final ClaimProposition PROPOSITION =
            new ClaimProposition(
                    ClaimSubject.of(
                            "company-registry",
                            "company-123"
                    ),
                    ClaimPredicate.of(
                            "has-director"
                    ),
                    new ClaimObject.Reference(
                            new ClaimReference(
                                    "person-registry",
                                    "person-456"
                            )
                    )
            );

    private static final ClaimDerivation DERIVATION =
            new ClaimDerivation(
                    ClaimDerivationKind.OBSERVED,
                    ClaimDerivationMethod.versionOne(
                            "company-director-extractor"
                    )
            );

    private static final ClaimLifecycleReason INVALID_REASON =
            new ClaimLifecycleReason(
                    "SOURCE_RETRACTION",
                    "The source retracted the assertion"
            );

    private static final ClaimLifecycleReason REINSTATE_REASON =
            new ClaimLifecycleReason(
                    "RETRACTION_REVERSED",
                    "The previous retraction was withdrawn"
            );

    private ClaimCreation createClaim() {
        return Claim.create(
                ClaimId.random(),
                PROPOSITION,
                ClaimOrigin.none(),
                DERIVATION,
                ClaimConfidence.of(0.95),
                null,
                null,
                T0
        );
    }

    @Test
    void newClaimShouldStartActiveAtRevisionOne() {
        ClaimCreation creation =
                createClaim();

        Claim claim =
                creation.claim();

        assertThat(claim.status())
                .isEqualTo(
                        ClaimStatus.ACTIVE
                );

        assertThat(claim.revision())
                .isEqualTo(
                        ClaimRevision.initial()
                );

        assertThat(claim.revisionHistory())
                .hasSize(1);

        assertThat(
                claim.revisionHistory()
                        .getFirst()
                        .status()
        ).isEqualTo(
                ClaimStatus.ACTIVE
        );
    }

    @Test
    void creationShouldProduceMatchingDomainEvent() {
        ClaimCreation creation =
                createClaim();

        assertThat(
                creation.event().claimId()
        ).isEqualTo(
                creation.claim().id()
        );

        assertThat(
                creation.event().revision()
        ).isEqualTo(
                ClaimRevision.initial()
        );

        assertThat(
                creation.event().occurredAt()
        ).isEqualTo(T0);
    }

    @Test
    void propositionShouldRemainExactlyAsCreated() {
        Claim claim =
                createClaim().claim();

        assertThat(claim.proposition())
                .isEqualTo(PROPOSITION);
    }

    @Test
    void claimIdentityShouldBeClaimIdNotProposition() {
        ClaimId firstId =
                ClaimId.random();

        ClaimId secondId =
                ClaimId.random();

        Claim first =
                Claim.create(
                        firstId,
                        PROPOSITION,
                        ClaimOrigin.none(),
                        DERIVATION,
                        null,
                        null,
                        null,
                        T0
                ).claim();

        Claim second =
                Claim.create(
                        secondId,
                        PROPOSITION,
                        ClaimOrigin.none(),
                        DERIVATION,
                        null,
                        null,
                        null,
                        T0
                ).claim();

        assertThat(first.proposition())
                .isEqualTo(second.proposition());

        assertThat(first)
                .isNotEqualTo(second);
    }

    @Test
    void sameClaimIdShouldDefineAggregateEquality() {
        ClaimId id =
                ClaimId.random();

        Claim first =
                Claim.create(
                        id,
                        PROPOSITION,
                        ClaimOrigin.none(),
                        DERIVATION,
                        null,
                        null,
                        null,
                        T0
                ).claim();

        Claim second =
                Claim.restore(
                        id,
                        PROPOSITION,
                        ClaimOrigin.none(),
                        DERIVATION,
                        null,
                        null,
                        null,
                        T0,
                        List.of(
                                ClaimLifecycleRevision
                                        .created(T0)
                        )
                );

        assertThat(first)
                .isEqualTo(second);

        assertThat(first.hashCode())
                .isEqualTo(second.hashCode());
    }

    @Test
    void claimMayExistWithoutConfidenceObservedAtOrValidTime() {
        Claim claim =
                Claim.create(
                        ClaimId.random(),
                        PROPOSITION,
                        ClaimOrigin.none(),
                        DERIVATION,
                        null,
                        null,
                        null,
                        T0
                ).claim();

        assertThat(claim.confidence())
                .isEmpty();

        assertThat(claim.observedAt())
                .isEmpty();

        assertThat(claim.validTime())
                .isEmpty();
    }

    @Test
    void supersessionShouldCreateNewRevisionAndBecomeTerminal() {
        Claim claim =
                createClaim().claim();

        ClaimId successor =
                ClaimId.random();

        ClaimSuperseded event =
                claim.supersedeBy(
                        successor,
                        null,
                        T1
                );

        assertThat(claim.status())
                .isEqualTo(
                        ClaimStatus.SUPERSEDED
                );

        assertThat(claim.revision())
                .isEqualTo(
                        new ClaimRevision(2)
                );

        assertThat(claim.revisionHistory())
                .hasSize(2);

        assertThat(event.claimId())
                .isEqualTo(claim.id());

        assertThat(event.successorClaimId())
                .isEqualTo(successor);
    }

    @Test
    void supersededClaimCannotBeInvalidated() {
        Claim claim =
                createClaim().claim();

        claim.supersedeBy(
                ClaimId.random(),
                null,
                T1
        );

        assertThatThrownBy(
                () -> claim.invalidate(
                        INVALID_REASON,
                        T2
                )
        )
                .isInstanceOf(
                        IllegalStateException.class
                )
                .hasMessageContaining(
                        "current status is SUPERSEDED"
                );
    }

    @Test
    void supersededClaimCannotBeReinstated() {
        Claim claim =
                createClaim().claim();

        claim.supersedeBy(
                ClaimId.random(),
                null,
                T1
        );

        assertThatThrownBy(
                () -> claim.reinstate(
                        REINSTATE_REASON,
                        T2
                )
        )
                .isInstanceOf(
                        IllegalStateException.class
                );
    }

    @Test
    void claimCannotSupersedeItself() {
        Claim claim =
                createClaim().claim();

        assertThatThrownBy(
                () -> claim.supersedeBy(
                        claim.id(),
                        null,
                        T1
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "Claim cannot supersede itself"
                );
    }

    @Test
    void invalidationShouldCreateRevisionAndPreserveReason() {
        Claim claim =
                createClaim().claim();

        ClaimInvalidated event =
                claim.invalidate(
                        INVALID_REASON,
                        T1
                );

        assertThat(claim.status())
                .isEqualTo(
                        ClaimStatus.INVALIDATED
                );

        assertThat(claim.revision())
                .isEqualTo(
                        new ClaimRevision(2)
                );

        assertThat(event.reason())
                .isEqualTo(INVALID_REASON);

        assertThat(
                claim.revisionHistory()
                        .getLast()
                        .reason()
        ).contains(INVALID_REASON);
    }

    @Test
    void invalidatedClaimCannotBeInvalidatedAgain() {
        Claim claim =
                createClaim().claim();

        claim.invalidate(
                INVALID_REASON,
                T1
        );

        assertThatThrownBy(
                () -> claim.invalidate(
                        INVALID_REASON,
                        T2
                )
        )
                .isInstanceOf(
                        IllegalStateException.class
                );
    }

    @Test
    void invalidatedClaimMayBeReinstated() {
        Claim claim =
                createClaim().claim();

        claim.invalidate(
                INVALID_REASON,
                T1
        );

        ClaimReinstated event =
                claim.reinstate(
                        REINSTATE_REASON,
                        T2
                );

        assertThat(claim.status())
                .isEqualTo(
                        ClaimStatus.ACTIVE
                );

        assertThat(claim.revision())
                .isEqualTo(
                        new ClaimRevision(3)
                );

        assertThat(event.reason())
                .isEqualTo(REINSTATE_REASON);

        assertThat(claim.revisionHistory())
                .hasSize(3);
    }

    @Test
    void reinstatementMustNotErasePreviousInvalidation() {
        Claim claim =
                createClaim().claim();

        claim.invalidate(
                INVALID_REASON,
                T1
        );

        claim.reinstate(
                REINSTATE_REASON,
                T2
        );

        assertThat(
                claim.revisionHistory()
                        .get(1)
                        .status()
        ).isEqualTo(
                ClaimStatus.INVALIDATED
        );

        assertThat(
                claim.revisionHistory()
                        .get(2)
                        .status()
        ).isEqualTo(
                ClaimStatus.ACTIVE
        );
    }

    @Test
    void reinstatedClaimMayLaterBeInvalidatedAgain() {
        Claim claim =
                createClaim().claim();

        claim.invalidate(
                INVALID_REASON,
                T1
        );

        claim.reinstate(
                REINSTATE_REASON,
                T2
        );

        claim.invalidate(
                INVALID_REASON,
                T3
        );

        assertThat(claim.status())
                .isEqualTo(
                        ClaimStatus.INVALIDATED
                );

        assertThat(claim.revision())
                .isEqualTo(
                        new ClaimRevision(4)
                );
    }

    @Test
    void lifecycleTimeMayEqualPreviousRevisionTime() {
        Claim claim =
                createClaim().claim();

        claim.invalidate(
                INVALID_REASON,
                T0
        );

        assertThat(claim.revision())
                .isEqualTo(
                        new ClaimRevision(2)
                );
    }

    @Test
    void lifecycleTimeCannotMoveBackwards() {
        Claim claim =
                createClaim().claim();

        claim.invalidate(
                INVALID_REASON,
                T2
        );

        assertThatThrownBy(
                () -> claim.reinstate(
                        REINSTATE_REASON,
                        T1
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessageContaining(
                        "must not precede"
                );
    }

    @Test
    void returnedRevisionHistoryShouldBeImmutable() {
        Claim claim =
                createClaim().claim();

        assertThatThrownBy(
                () -> claim.revisionHistory()
                        .clear()
        )
                .isInstanceOf(
                        UnsupportedOperationException.class
                );
    }

    @Test
    void restorationShouldPreserveValidHistory() {
        ClaimId id =
                ClaimId.random();

        List<ClaimLifecycleRevision> history =
                List.of(
                        ClaimLifecycleRevision.created(T0),
                        ClaimLifecycleRevision.invalidated(
                                new ClaimRevision(2),
                                T1,
                                INVALID_REASON
                        ),
                        ClaimLifecycleRevision.reinstated(
                                new ClaimRevision(3),
                                T2,
                                REINSTATE_REASON
                        )
                );

        Claim claim =
                Claim.restore(
                        id,
                        PROPOSITION,
                        ClaimOrigin.none(),
                        DERIVATION,
                        null,
                        null,
                        null,
                        T0,
                        history
                );

        assertThat(claim.status())
                .isEqualTo(
                        ClaimStatus.ACTIVE
                );

        assertThat(claim.revision())
                .isEqualTo(
                        new ClaimRevision(3)
                );

        assertThat(claim.revisionHistory())
                .containsExactlyElementsOf(history);
    }

    @Test
    void restorationShouldDefensivelyCopyHistory() {
        List<ClaimLifecycleRevision> history =
                new ArrayList<>();

        history.add(
                ClaimLifecycleRevision.created(T0)
        );

        Claim claim =
                Claim.restore(
                        ClaimId.random(),
                        PROPOSITION,
                        ClaimOrigin.none(),
                        DERIVATION,
                        null,
                        null,
                        null,
                        T0,
                        history
                );

        history.add(
                ClaimLifecycleRevision.invalidated(
                        new ClaimRevision(2),
                        T1,
                        INVALID_REASON
                )
        );

        assertThat(claim.revisionHistory())
                .hasSize(1);
    }

    @Test
    void restorationShouldRejectEmptyHistory() {
        assertThatThrownBy(
                () -> Claim.restore(
                        ClaimId.random(),
                        PROPOSITION,
                        ClaimOrigin.none(),
                        DERIVATION,
                        null,
                        null,
                        null,
                        T0,
                        List.of()
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "revisionHistory must not be empty"
                );
    }

    @Test
    void restorationShouldRejectHistoryWhoseFirstEntryIsNotCreated() {
        List<ClaimLifecycleRevision> history =
                List.of(
                        ClaimLifecycleRevision.invalidated(
                                new ClaimRevision(2),
                                T0,
                                INVALID_REASON
                        )
                );

        assertThatThrownBy(
                () -> Claim.restore(
                        ClaimId.random(),
                        PROPOSITION,
                        ClaimOrigin.none(),
                        DERIVATION,
                        null,
                        null,
                        null,
                        T0,
                        history
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "first Claim revision must be CREATED"
                );
    }

    @Test
    void restorationShouldRejectCreatedAtMismatch() {
        List<ClaimLifecycleRevision> history =
                List.of(
                        ClaimLifecycleRevision.created(T1)
                );

        assertThatThrownBy(
                () -> Claim.restore(
                        ClaimId.random(),
                        PROPOSITION,
                        ClaimOrigin.none(),
                        DERIVATION,
                        null,
                        null,
                        null,
                        T0,
                        history
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "createdAt must equal the CREATED revision time"
                );
    }

    @Test
    void restorationShouldRejectNonContiguousRevisions() {
        List<ClaimLifecycleRevision> history =
                List.of(
                        ClaimLifecycleRevision.created(T0),
                        ClaimLifecycleRevision.invalidated(
                                new ClaimRevision(3),
                                T1,
                                INVALID_REASON
                        )
                );

        assertThatThrownBy(
                () -> Claim.restore(
                        ClaimId.random(),
                        PROPOSITION,
                        ClaimOrigin.none(),
                        DERIVATION,
                        null,
                        null,
                        null,
                        T0,
                        history
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "Claim revisions must be contiguous"
                );
    }

    @Test
    void restorationShouldRejectTemporalRegression() {
        List<ClaimLifecycleRevision> history =
                List.of(
                        ClaimLifecycleRevision.created(T2),
                        ClaimLifecycleRevision.invalidated(
                                new ClaimRevision(2),
                                T1,
                                INVALID_REASON
                        )
                );

        assertThatThrownBy(
                () -> Claim.restore(
                        ClaimId.random(),
                        PROPOSITION,
                        ClaimOrigin.none(),
                        DERIVATION,
                        null,
                        null,
                        null,
                        T2,
                        history
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "Claim revision times must be non-decreasing"
                );
    }

    @Test
    void restorationShouldRejectReinstatementDirectlyFromActive() {
        List<ClaimLifecycleRevision> history =
                List.of(
                        ClaimLifecycleRevision.created(T0),
                        ClaimLifecycleRevision.reinstated(
                                new ClaimRevision(2),
                                T1,
                                REINSTATE_REASON
                        )
                );

        assertThatThrownBy(
                () -> Claim.restore(
                        ClaimId.random(),
                        PROPOSITION,
                        ClaimOrigin.none(),
                        DERIVATION,
                        null,
                        null,
                        null,
                        T0,
                        history
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                );
    }

    @Test
    void restorationShouldRejectTransitionAfterSupersession() {
        List<ClaimLifecycleRevision> history =
                List.of(
                        ClaimLifecycleRevision.created(T0),
                        ClaimLifecycleRevision.superseded(
                                new ClaimRevision(2),
                                T1,
                                ClaimId.random(),
                                null
                        ),
                        ClaimLifecycleRevision.invalidated(
                                new ClaimRevision(3),
                                T2,
                                INVALID_REASON
                        )
                );

        assertThatThrownBy(
                () -> Claim.restore(
                        ClaimId.random(),
                        PROPOSITION,
                        ClaimOrigin.none(),
                        DERIVATION,
                        null,
                        null,
                        null,
                        T0,
                        history
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "SUPERSEDED Claim is terminal"
                );
    }

    @Test
    void restorationShouldRejectSelfSupersession() {
        ClaimId id =
                ClaimId.random();

        List<ClaimLifecycleRevision> history =
                List.of(
                        ClaimLifecycleRevision.created(T0),
                        ClaimLifecycleRevision.superseded(
                                new ClaimRevision(2),
                                T1,
                                id,
                                null
                        )
                );

        assertThatThrownBy(
                () -> Claim.restore(
                        id,
                        PROPOSITION,
                        ClaimOrigin.none(),
                        DERIVATION,
                        null,
                        null,
                        null,
                        T0,
                        history
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "Claim cannot reference itself as successor"
                );
    }
}