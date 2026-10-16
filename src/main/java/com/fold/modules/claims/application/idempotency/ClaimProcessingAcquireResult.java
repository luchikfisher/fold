package com.fold.modules.claims.application.idempotency;

import java.util.Objects;

/**
 * Result of atomically attempting to acquire one semantic Claim-processing
 * operation.
 */
public sealed interface ClaimProcessingAcquireResult
        permits ClaimProcessingAcquireResult.Acquired,
        ClaimProcessingAcquireResult.AlreadyCompleted {

    /**
     * The caller acquired responsibility for performing this processing
     * operation.
     */
    record Acquired()
            implements ClaimProcessingAcquireResult {
    }

    /**
     * The same semantic processing operation was completed previously.
     *
     * @param receipt authoritative previous processing result
     */
    record AlreadyCompleted(
            ClaimProcessingReceipt receipt
    ) implements ClaimProcessingAcquireResult {

        public AlreadyCompleted {
            Objects.requireNonNull(
                    receipt,
                    "receipt must not be null"
            );
        }
    }
}