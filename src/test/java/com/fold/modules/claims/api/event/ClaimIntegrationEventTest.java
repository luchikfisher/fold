package com.fold.modules.claims.api.event;

import com.fold.kernel.events.EventVersion;
import com.fold.kernel.ids.ClaimId;
import com.fold.kernel.time.Timestamp;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClaimIntegrationEventTest {

    private static final Timestamp NOW =
            Timestamp.parse(
                    "2026-10-03T10:00:00Z"
            );

    @Test
    void claimCreatedV1ShouldExposeStableContract() {
        ClaimId claimId =
                ClaimId.random();

        ClaimCreatedV1 event =
                new ClaimCreatedV1(
                        claimId,
                        NOW
                );

        assertThat(event.claimId())
                .isEqualTo(claimId);

        assertThat(event.occurredAt())
                .isEqualTo(NOW);

        assertThat(event.eventType())
                .isEqualTo(
                        "claims.claim-created"
                );

        assertThat(event.version())
                .isEqualTo(
                        EventVersion.initial()
                );
    }

    @Test
    void claimCreatedShouldRejectNullClaimId() {
        assertThatThrownBy(
                () -> new ClaimCreatedV1(
                        null,
                        NOW
                )
        )
                .isInstanceOf(
                        NullPointerException.class
                )
                .hasMessage(
                        "claimId must not be null"
                );
    }

    @Test
    void claimCreatedShouldRejectNullOccurredAt() {
        assertThatThrownBy(
                () -> new ClaimCreatedV1(
                        ClaimId.random(),
                        null
                )
        )
                .isInstanceOf(
                        NullPointerException.class
                )
                .hasMessage(
                        "occurredAt must not be null"
                );
    }
}