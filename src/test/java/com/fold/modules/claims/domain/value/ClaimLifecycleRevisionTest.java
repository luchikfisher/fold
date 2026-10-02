package com.fold.modules.claims.domain.value;

import com.fold.kernel.ids.ClaimId;
import com.fold.kernel.time.Timestamp;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClaimLifecycleRevisionTest {

    private static final Timestamp NOW =
            Timestamp.parse(
                    "2026-10-02T10:00:00Z"
            );

    private static final ClaimLifecycleReason REASON =
            new ClaimLifecycleReason(
                    "SOURCE_CORRECTION",
                    "Source corrected the assertion"
            );

    @Test
    void createdRevisionShouldBeInitialAndActive() {
        ClaimLifecycleRevision revision =
                ClaimLifecycleRevision.created(NOW);

        assertThat(revision.revision())
                .isEqualTo(
                        ClaimRevision.initial()
                );

        assertThat(revision.transition())
                .isEqualTo(
                        ClaimLifecycleTransition.CREATED
                );

        assertThat(revision.status())
                .isEqualTo(
                        ClaimStatus.ACTIVE
                );

        assertThat(revision.relatedClaimId())
                .isEmpty();

        assertThat(revision.reason())
                .isEmpty();
    }

    @Test
    void supersededRevisionShouldReferenceSuccessor() {
        ClaimId successor =
                ClaimId.random();

        ClaimLifecycleRevision revision =
                ClaimLifecycleRevision.superseded(
                        new ClaimRevision(2),
                        NOW,
                        successor,
                        REASON
                );

        assertThat(revision.status())
                .isEqualTo(
                        ClaimStatus.SUPERSEDED
                );

        assertThat(revision.relatedClaimId())
                .contains(successor);

        assertThat(revision.reason())
                .contains(REASON);
    }

    @Test
    void supersessionReasonMayBeAbsent() {
        ClaimLifecycleRevision revision =
                ClaimLifecycleRevision.superseded(
                        new ClaimRevision(2),
                        NOW,
                        ClaimId.random(),
                        null
                );

        assertThat(revision.reason()).isEmpty();
    }

    @Test
    void invalidationShouldRequireReason() {
        assertThatThrownBy(
                () -> ClaimLifecycleRevision
                        .invalidated(
                                new ClaimRevision(2),
                                NOW,
                                null
                        )
        ).isInstanceOf(
                NullPointerException.class
        );
    }

    @Test
    void reinstatementShouldReturnActiveStatus() {
        ClaimLifecycleRevision revision =
                ClaimLifecycleRevision.reinstated(
                        new ClaimRevision(3),
                        NOW,
                        REASON
                );

        assertThat(revision.transition())
                .isEqualTo(
                        ClaimLifecycleTransition.REINSTATED
                );

        assertThat(revision.status())
                .isEqualTo(
                        ClaimStatus.ACTIVE
                );
    }

    @Test
    void restorationShouldRejectInvalidTransitionStatusPair() {
        assertThatThrownBy(
                () -> ClaimLifecycleRevision.restore(
                        new ClaimRevision(2),
                        ClaimLifecycleTransition.INVALIDATED,
                        ClaimStatus.ACTIVE,
                        NOW,
                        null,
                        REASON
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "INVALIDATED transition must result in INVALIDATED status"
                );
    }

    @Test
    void restorationShouldRejectCreatedRevisionOtherThanOne() {
        assertThatThrownBy(
                () -> ClaimLifecycleRevision.restore(
                        new ClaimRevision(2),
                        ClaimLifecycleTransition.CREATED,
                        ClaimStatus.ACTIVE,
                        NOW,
                        null,
                        null
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                );
    }
}