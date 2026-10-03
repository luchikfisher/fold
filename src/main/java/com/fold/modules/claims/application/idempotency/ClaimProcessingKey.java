package com.fold.modules.claims.application.idempotency;

import com.fold.kernel.ids.ObservationId;
import com.fold.modules.claims.domain.value.ClaimDerivation;

import java.util.Objects;

/**
 * Identifies one semantic Observation-to-Claims processing operation.
 *
 * <p>The processing identity is defined by:</p>
 *
 * <pre>
 * authoritative Observation
 * +
 * semantic Claim derivation
 * </pre>
 *
 * <p>The same Observation processed using the same derivation kind, method,
 * and method version has the same processing key regardless of event delivery
 * identity, correlation metadata, retry count, or execution time.</p>
 *
 * <p>This value is deliberately not a Claim identity and is deliberately not a
 * proposition identity. One processing operation may produce zero, one, or
 * many Claims.</p>
 *
 * <p>A derivation implementation whose semantics change must use a new
 * {@link com.fold.modules.claims.domain.value.ClaimDerivationMethod} version.
 * Otherwise FOLD would be unable to distinguish a legitimate reprocessing
 * operation from a retry of previously completed work.</p>
 *
 * @param observationId authoritative upstream Observation
 * @param derivation    semantic derivation being executed
 */
public record ClaimProcessingKey(
        ObservationId observationId,
        ClaimDerivation derivation
) {

    public ClaimProcessingKey {
        Objects.requireNonNull(
                observationId,
                "observationId must not be null"
        );

        Objects.requireNonNull(
                derivation,
                "derivation must not be null"
        );
    }
}