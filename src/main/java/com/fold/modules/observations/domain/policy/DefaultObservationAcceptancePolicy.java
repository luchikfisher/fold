package com.fold.modules.observations.domain.policy;

import com.fold.modules.observations.domain.model.Observation;

import java.util.Objects;

/**
 * Version-one observation acceptance policy.
 *
 * <p>The current observations domain defines no semantic rejection rule beyond
 * the structural invariants already enforced when its value objects and
 * aggregate are constructed. Consequently, every structurally valid received
 * observation is accepted.</p>
 *
 * <p>This explicit policy preserves the admission decision as a domain
 * boundary without inventing rules that FOLD does not yet require. Future
 * semantic rejection rules may replace or compose this policy without moving
 * admission logic into the application layer.</p>
 */
public final class DefaultObservationAcceptancePolicy
        implements ObservationAcceptancePolicy {

    @Override
    public ObservationAcceptanceDecision evaluate(
            Observation observation
    ) {
        Objects.requireNonNull(
                observation,
                "observation must not be null"
        );

        return ObservationAcceptanceDecision.accepted();
    }
}