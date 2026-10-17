package com.fold.modules.claims.application.feature.deriveclaims;

import com.fold.modules.claims.application.idempotency.ClaimProcessingKey;
import com.fold.modules.claims.domain.derivation.ClaimProposal;
import com.fold.modules.claims.domain.value.ClaimDerivation;

import java.util.List;
import java.util.Objects;

/**
 * Represents the complete result of one semantic derivation operation.
 *
 * <p>The batch exists even when no Claims were proposed so downstream
 * processing can durably record successful zero-output processing.</p>
 */
public record ClaimDerivationBatch(
        ClaimProcessingKey processingKey,
        List<ClaimProposal> proposals
) {

    public ClaimDerivationBatch {
        Objects.requireNonNull(
                processingKey,
                "processingKey must not be null"
        );

        Objects.requireNonNull(
                proposals,
                "proposals must not be null"
        );

        if (proposals.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException(
                    "proposals must not contain null"
            );
        }

        proposals = List.copyOf(proposals);
    }

    public ClaimDerivation derivation() {
        return processingKey.derivation();
    }

    public boolean isEmpty() {
        return proposals.isEmpty();
    }

    public int size() {
        return proposals.size();
    }
}