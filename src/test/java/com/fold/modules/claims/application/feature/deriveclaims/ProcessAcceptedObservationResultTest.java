package com.fold.modules.claims.application.feature.deriveclaims;

import com.fold.kernel.ids.ClaimId;
import com.fold.kernel.ids.ObservationId;
import com.fold.kernel.time.Timestamp;
import com.fold.modules.claims.application.idempotency.ClaimProcessingKey;
import com.fold.modules.claims.application.idempotency.ClaimProcessingReceipt;
import com.fold.modules.claims.domain.value.ClaimDerivation;
import com.fold.modules.claims.domain.value.ClaimDerivationKind;
import com.fold.modules.claims.domain.value.ClaimDerivationMethod;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ProcessAcceptedObservationResultTest {

    @Test
    void shouldDistinguishNewAndPreviouslyCompletedBatches() {
        ClaimId newClaim =
                ClaimId.random();

        ClaimId oldClaim =
                ClaimId.random();

        ProcessAcceptedObservationResult result =
                new ProcessAcceptedObservationResult(
                        List.of(
                                new ProcessAcceptedObservationResult.Completed(
                                        receipt(
                                                "new",
                                                List.of(
                                                        newClaim
                                                )
                                        )
                                ),
                                new ProcessAcceptedObservationResult.AlreadyCompleted(
                                        receipt(
                                                "old",
                                                List.of(
                                                        oldClaim
                                                )
                                        )
                                )
                        )
                );

        assertThat(result.completedNowCount())
                .isEqualTo(1);

        assertThat(result.alreadyCompletedCount())
                .isEqualTo(1);

        assertThat(result.newlyCreatedClaimIds())
                .containsExactly(
                        newClaim
                );
    }

    @Test
    void zeroOutputCompletedBatchShouldStillCountAsCompletedNow() {
        ProcessAcceptedObservationResult result =
                new ProcessAcceptedObservationResult(
                        List.of(
                                new ProcessAcceptedObservationResult.Completed(
                                        receipt(
                                                "zero",
                                                List.of()
                                        )
                                )
                        )
                );

        assertThat(result.completedNowCount())
                .isEqualTo(1);

        assertThat(result.newlyCreatedClaimIds())
                .isEmpty();
    }

    private static ClaimProcessingReceipt receipt(
            String method,
            List<ClaimId> ids
    ) {
        return new ClaimProcessingReceipt(
                new ClaimProcessingKey(
                        ObservationId.random(),
                        new ClaimDerivation(
                                ClaimDerivationKind.OBSERVED,
                                new ClaimDerivationMethod(
                                        method,
                                        1
                                )
                        )
                ),
                Timestamp.parse(
                        "2026-10-03T10:00:00Z"
                ),
                ids
        );
    }
}