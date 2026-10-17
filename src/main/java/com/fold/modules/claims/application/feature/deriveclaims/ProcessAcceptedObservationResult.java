package com.fold.modules.claims.application.feature.deriveclaims;

import com.fold.kernel.ids.ClaimId;
import com.fold.modules.claims.application.idempotency.ClaimProcessingReceipt;

import java.util.List;
import java.util.Objects;

/**
 * Result of authoritative processing of one accepted Observation.
 */
public record ProcessAcceptedObservationResult(
        List<BatchResult> batches
) {

    public ProcessAcceptedObservationResult {
        Objects.requireNonNull(
                batches,
                "batches must not be null"
        );

        if (batches.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException(
                    "batches must not contain null"
            );
        }

        batches = List.copyOf(batches);
    }

    public sealed interface BatchResult
            permits Completed,
            AlreadyCompleted {

        ClaimProcessingReceipt receipt();
    }

    /**
     * Processing was newly performed in this invocation.
     */
    public record Completed(
            ClaimProcessingReceipt receipt
    ) implements BatchResult {

        public Completed {
            Objects.requireNonNull(
                    receipt,
                    "receipt must not be null"
            );
        }
    }

    /**
     * Processing had already completed previously.
     */
    public record AlreadyCompleted(
            ClaimProcessingReceipt receipt
    ) implements BatchResult {

        public AlreadyCompleted {
            Objects.requireNonNull(
                    receipt,
                    "receipt must not be null"
            );
        }
    }

    public List<ClaimId> newlyCreatedClaimIds() {
        return batches.stream()
                .filter(Completed.class::isInstance)
                .map(Completed.class::cast)
                .flatMap(result ->
                        result.receipt()
                                .producedClaimIds()
                                .stream()
                )
                .toList();
    }

    public int completedNowCount() {
        return (int) batches.stream()
                .filter(Completed.class::isInstance)
                .count();
    }

    public int alreadyCompletedCount() {
        return (int) batches.stream()
                .filter(AlreadyCompleted.class::isInstance)
                .count();
    }
}