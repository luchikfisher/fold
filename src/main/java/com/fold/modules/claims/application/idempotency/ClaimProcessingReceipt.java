package com.fold.modules.claims.application.idempotency;

import com.fold.kernel.ids.ClaimId;
import com.fold.kernel.time.Timestamp;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;

/**
 * Records the completed result of one idempotent Claim-processing operation.
 *
 * <p>A receipt establishes that a {@link ClaimProcessingKey} has already been
 * processed successfully. The operation may have produced zero, one, or many
 * Claims.</p>
 *
 * <p>An empty {@code producedClaimIds} collection is therefore meaningful. It
 * records a successfully completed derivation that legitimately produced no
 * Claims and prevents repeated delivery from continually re-executing it.</p>
 *
 * <p>The receipt is processing metadata. It is not part of Claim identity,
 * proposition equivalence, Claim provenance, or Claim lifecycle.</p>
 *
 * @param key              semantic processing identity
 * @param processedAt      time at which processing successfully completed
 * @param producedClaimIds Claims created by this processing operation,
 *                         possibly empty
 */
public record ClaimProcessingReceipt(
        ClaimProcessingKey key,
        Timestamp processedAt,
        List<ClaimId> producedClaimIds
) {

    public ClaimProcessingReceipt {
        Objects.requireNonNull(
                key,
                "key must not be null"
        );

        Objects.requireNonNull(
                processedAt,
                "processedAt must not be null"
        );

        Objects.requireNonNull(
                producedClaimIds,
                "producedClaimIds must not be null"
        );

        if (producedClaimIds.stream()
                .anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException(
                    "producedClaimIds must not contain null"
            );
        }

        if (new HashSet<>(producedClaimIds).size()
                != producedClaimIds.size()) {
            throw new IllegalArgumentException(
                    "producedClaimIds must not contain duplicates"
            );
        }

        producedClaimIds =
                List.copyOf(producedClaimIds);
    }

    public boolean producedClaims() {
        return !producedClaimIds.isEmpty();
    }

    public int producedClaimCount() {
        return producedClaimIds.size();
    }
}