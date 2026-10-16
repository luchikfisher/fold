package com.fold.modules.claims.application.idempotency;

import com.fold.kernel.ids.ClaimId;
import com.fold.kernel.ids.ObservationId;
import com.fold.kernel.time.Timestamp;
import com.fold.modules.claims.domain.value.ClaimDerivation;
import com.fold.modules.claims.domain.value.ClaimDerivationKind;
import com.fold.modules.claims.domain.value.ClaimDerivationMethod;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClaimProcessingReceiptTest {

    private static final Timestamp PROCESSED_AT =
            Timestamp.parse(
                    "2026-10-03T12:00:00Z"
            );

    @Test
    void zeroOutputProcessingShouldStillBeCompleteResult() {
        ClaimProcessingReceipt receipt =
                new ClaimProcessingReceipt(
                        key(),
                        PROCESSED_AT,
                        List.of()
                );

        assertThat(receipt.producedClaims())
                .isFalse();

        assertThat(receipt.producedClaimCount())
                .isZero();

        assertThat(receipt.producedClaimIds())
                .isEmpty();

        assertThat(receipt.processedAt())
                .isEqualTo(PROCESSED_AT);
    }

    @Test
    void shouldRepresentCompletedProcessingWithProducedClaims() {
        ClaimId first =
                ClaimId.random();

        ClaimId second =
                ClaimId.random();

        ClaimProcessingReceipt receipt =
                new ClaimProcessingReceipt(
                        key(),
                        PROCESSED_AT,
                        List.of(
                                first,
                                second
                        )
                );

        assertThat(receipt.producedClaimIds())
                .containsExactly(
                        first,
                        second
                );

        assertThat(receipt.producedClaims())
                .isTrue();

        assertThat(receipt.producedClaimCount())
                .isEqualTo(2);

        assertThat(receipt.processedAt())
                .isEqualTo(PROCESSED_AT);
    }

    @Test
    void shouldPreserveProducedClaimOrder() {
        ClaimId first =
                ClaimId.random();

        ClaimId second =
                ClaimId.random();

        ClaimId third =
                ClaimId.random();

        ClaimProcessingReceipt receipt =
                new ClaimProcessingReceipt(
                        key(),
                        PROCESSED_AT,
                        List.of(
                                first,
                                second,
                                third
                        )
                );

        assertThat(receipt.producedClaimIds())
                .containsExactly(
                        first,
                        second,
                        third
                );
    }

    @Test
    void shouldDefensivelyCopyClaimIds() {
        List<ClaimId> ids =
                new ArrayList<>();

        ids.add(
                ClaimId.random()
        );

        ClaimProcessingReceipt receipt =
                new ClaimProcessingReceipt(
                        key(),
                        PROCESSED_AT,
                        ids
                );

        ids.clear();

        assertThat(receipt.producedClaimIds())
                .hasSize(1);
    }

    @Test
    void producedClaimIdsShouldBeImmutable() {
        ClaimProcessingReceipt receipt =
                new ClaimProcessingReceipt(
                        key(),
                        PROCESSED_AT,
                        List.of(
                                ClaimId.random()
                        )
                );

        assertThatThrownBy(
                () -> receipt
                        .producedClaimIds()
                        .clear()
        )
                .isInstanceOf(
                        UnsupportedOperationException.class
                );
    }

    @Test
    void duplicateProducedClaimIdsShouldBeRejected() {
        ClaimId id =
                ClaimId.random();

        assertThatThrownBy(
                () -> new ClaimProcessingReceipt(
                        key(),
                        PROCESSED_AT,
                        List.of(
                                id,
                                id
                        )
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "producedClaimIds must not contain duplicates"
                );
    }

    @Test
    void nullProducedClaimIdShouldBeRejected() {
        List<ClaimId> ids =
                new ArrayList<>();

        ids.add(
                ClaimId.random()
        );

        ids.add(null);

        assertThatThrownBy(
                () -> new ClaimProcessingReceipt(
                        key(),
                        PROCESSED_AT,
                        ids
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "producedClaimIds must not contain null"
                );
    }

    @Test
    void nullKeyShouldBeRejected() {
        assertThatThrownBy(
                () -> new ClaimProcessingReceipt(
                        null,
                        PROCESSED_AT,
                        List.of()
                )
        )
                .isInstanceOf(
                        NullPointerException.class
                )
                .hasMessage(
                        "key must not be null"
                );
    }

    @Test
    void nullProcessingTimeShouldBeRejected() {
        assertThatThrownBy(
                () -> new ClaimProcessingReceipt(
                        key(),
                        null,
                        List.of()
                )
        )
                .isInstanceOf(
                        NullPointerException.class
                )
                .hasMessage(
                        "processedAt must not be null"
                );
    }

    @Test
    void nullProducedClaimCollectionShouldBeRejected() {
        assertThatThrownBy(
                () -> new ClaimProcessingReceipt(
                        key(),
                        PROCESSED_AT,
                        null
                )
        )
                .isInstanceOf(
                        NullPointerException.class
                )
                .hasMessage(
                        "producedClaimIds must not be null"
                );
    }

    private static ClaimProcessingKey key() {
        return new ClaimProcessingKey(
                ObservationId.random(),
                new ClaimDerivation(
                        ClaimDerivationKind.OBSERVED,
                        new ClaimDerivationMethod(
                                "test-rule",
                                1
                        )
                )
        );
    }
}