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
                    "2026-10-02T12:00:00Z"
            );

    private static ClaimProcessingKey key() {
        return new ClaimProcessingKey(
                ObservationId.random(),
                new ClaimDerivation(
                        ClaimDerivationKind.OBSERVED,
                        new ClaimDerivationMethod(
                                "company-registration-extractor",
                                1
                        )
                )
        );
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
                        List.of(first, second)
                );

        assertThat(receipt.processedAt())
                .isEqualTo(PROCESSED_AT);

        assertThat(receipt.producedClaimIds())
                .containsExactly(
                        first,
                        second
                );

        assertThat(receipt.producedClaims())
                .isTrue();

        assertThat(receipt.producedClaimCount())
                .isEqualTo(2);
    }

    @Test
    void zeroClaimDerivationShouldStillProduceValidReceipt() {
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
    }

    @Test
    void shouldDefensivelyCopyProducedClaimIds() {
        List<ClaimId> claimIds =
                new ArrayList<>();

        claimIds.add(
                ClaimId.random()
        );

        ClaimProcessingReceipt receipt =
                new ClaimProcessingReceipt(
                        key(),
                        PROCESSED_AT,
                        claimIds
                );

        claimIds.clear();

        assertThat(receipt.producedClaimIds())
                .hasSize(1);
    }

    @Test
    void exposedClaimIdsShouldBeImmutable() {
        ClaimProcessingReceipt receipt =
                new ClaimProcessingReceipt(
                        key(),
                        PROCESSED_AT,
                        List.of(
                                ClaimId.random()
                        )
                );

        assertThatThrownBy(
                () -> receipt.producedClaimIds()
                        .clear()
        )
                .isInstanceOf(
                        UnsupportedOperationException.class
                );
    }

    @Test
    void shouldRejectDuplicateClaimIds() {
        ClaimId claimId =
                ClaimId.random();

        assertThatThrownBy(
                () -> new ClaimProcessingReceipt(
                        key(),
                        PROCESSED_AT,
                        List.of(
                                claimId,
                                claimId
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
    void shouldRejectNullClaimId() {
        List<ClaimId> values =
                new ArrayList<>();

        values.add(
                ClaimId.random()
        );

        values.add(null);

        assertThatThrownBy(
                () -> new ClaimProcessingReceipt(
                        key(),
                        PROCESSED_AT,
                        values
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
    void shouldRejectNullKey() {
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
    void shouldRejectNullProcessingTime() {
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
    void shouldRejectNullClaimCollection() {
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
}