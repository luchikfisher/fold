package com.fold.modules.claims.domain.event;

import com.fold.kernel.ids.ClaimId;
import com.fold.kernel.time.Timestamp;
import com.fold.modules.claims.domain.value.ClaimLifecycleReason;
import com.fold.modules.claims.domain.value.ClaimRevision;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClaimDomainEventTest {

    private static final Timestamp NOW =
            Timestamp.parse(
                    "2026-10-02T10:00:00Z"
            );

    private static final ClaimLifecycleReason REASON =
            new ClaimLifecycleReason(
                    "CORRECTION",
                    "Correction received"
            );

    @Test
    void createdEventShouldRequireInitialRevision() {
        ClaimCreated event =
                new ClaimCreated(
                        ClaimId.random(),
                        ClaimRevision.initial(),
                        NOW
                );

        assertThat(event.revision())
                .isEqualTo(
                        ClaimRevision.initial()
                );
    }

    @Test
    void createdEventShouldRejectLaterRevision() {
        assertThatThrownBy(
                () -> new ClaimCreated(
                        ClaimId.random(),
                        new ClaimRevision(2),
                        NOW
                )
        ).isInstanceOf(
                IllegalArgumentException.class
        );
    }

    @Test
    void supersededEventShouldPreserveSuccessor() {
        ClaimId claimId =
                ClaimId.random();

        ClaimId successor =
                ClaimId.random();

        ClaimSuperseded event =
                new ClaimSuperseded(
                        claimId,
                        successor,
                        new ClaimRevision(2),
                        REASON,
                        NOW
                );

        assertThat(event.claimId())
                .isEqualTo(claimId);

        assertThat(event.successorClaimId())
                .isEqualTo(successor);

        assertThat(event.reason())
                .contains(REASON);
    }

    @Test
    void supersededEventShouldRejectSelfReference() {
        ClaimId claimId =
                ClaimId.random();

        assertThatThrownBy(
                () -> new ClaimSuperseded(
                        claimId,
                        claimId,
                        new ClaimRevision(2),
                        null,
                        NOW
                )
        ).isInstanceOf(
                IllegalArgumentException.class
        );
    }

    @Test
    void invalidatedEventShouldRequireReason() {
        assertThatThrownBy(
                () -> new ClaimInvalidated(
                        ClaimId.random(),
                        new ClaimRevision(2),
                        null,
                        NOW
                )
        ).isInstanceOf(
                NullPointerException.class
        );
    }

    @Test
    void reinstatedEventShouldPreserveReason() {
        ClaimReinstated event =
                new ClaimReinstated(
                        ClaimId.random(),
                        new ClaimRevision(3),
                        REASON,
                        NOW
                );

        assertThat(event.reason())
                .isEqualTo(REASON);
    }
}