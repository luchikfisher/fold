package com.fold.modules.claims.application.idempotency;

/**
 * Persistence boundary for semantic Claim-processing idempotency.
 *
 * <p>{@link #acquire(ClaimProcessingKey)} must be atomic. Implementations must
 * not implement acquisition as an application-level exists-then-insert
 * sequence.</p>
 */
public interface ClaimProcessingRepository {

    /**
     * Atomically acquires one semantic processing operation or returns its
     * previously completed receipt.
     */
    ClaimProcessingAcquireResult acquire(
            ClaimProcessingKey key
    );

    /**
     * Marks an acquired operation as successfully completed.
     *
     * <p>This operation must execute in the same transaction as all Claim
     * writes and outbox writes produced by the processing operation.</p>
     */
    void complete(
            ClaimProcessingReceipt receipt
    );
}