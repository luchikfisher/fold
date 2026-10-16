package com.fold.modules.claims.application.idempotency;

import com.fold.kernel.ids.ObservationId;
import com.fold.kernel.time.Timestamp;
import com.fold.modules.claims.domain.value.ClaimDerivation;
import com.fold.modules.claims.domain.value.ClaimDerivationKind;
import com.fold.modules.claims.domain.value.ClaimDerivationMethod;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClaimProcessingAcquireResultTest {

    @Test
    void acquiredShouldRepresentNewProcessingOwnership() {
        ClaimProcessingAcquireResult result =
                new ClaimProcessingAcquireResult.Acquired();

        assertThat(result)
                .isInstanceOf(
                        ClaimProcessingAcquireResult.Acquired.class
                );
    }

    @Test
    void alreadyCompletedShouldPreserveReceipt() {
        ClaimProcessingReceipt receipt =
                receipt();

        ClaimProcessingAcquireResult.AlreadyCompleted result =
                new ClaimProcessingAcquireResult.AlreadyCompleted(
                        receipt
                );

        assertThat(result.receipt())
                .isSameAs(receipt);
    }

    @Test
    void alreadyCompletedShouldRejectNullReceipt() {
        assertThatThrownBy(
                () -> new ClaimProcessingAcquireResult.AlreadyCompleted(
                        null
                )
        )
                .isInstanceOf(
                        NullPointerException.class
                )
                .hasMessage(
                        "receipt must not be null"
                );
    }

    private static ClaimProcessingReceipt receipt() {
        return new ClaimProcessingReceipt(
                new ClaimProcessingKey(
                        ObservationId.random(),
                        new ClaimDerivation(
                                ClaimDerivationKind.OBSERVED,
                                new ClaimDerivationMethod(
                                        "test",
                                        1
                                )
                        )
                ),
                Timestamp.parse(
                        "2026-10-03T10:00:00Z"
                ),
                List.of()
        );
    }
}