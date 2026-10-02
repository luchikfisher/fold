package com.fold.modules.observations.domain.policy;

import com.fold.modules.observations.domain.model.Observation;

/**
 * Decides whether a structurally valid observation may enter downstream
 * knowledge processing.
 *
 * <p>Structural validity is enforced by the observation domain model itself.
 * This policy exists for semantic admission rules that cannot be expressed as
 * construction invariants.</p>
 *
 * <p>The policy must not mutate the observation. The resulting decision is
 * applied by the aggregate through its own lifecycle operations.</p>
 */
@FunctionalInterface
public interface ObservationAcceptancePolicy {

    /**
     * Evaluates an observation that is currently in the {@code RECEIVED}
     * state.
     *
     * @param observation the observation to evaluate
     * @return the admission decision
     */
    ObservationAcceptanceDecision evaluate(
            Observation observation
    );
}