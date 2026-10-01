package com.fold.modules.observations.domain.value;

import com.fold.kernel.ids.ObservationId;

import java.util.Objects;

/**
 * Represents the outcome of checking an observation fingerprint against the
 * authoritative observation store.
 *
 * <p>A unique result means no existing observation currently owns the
 * fingerprint. A duplicate result identifies the observation that already
 * represents the same semantic input.</p>
 */
public sealed interface ObservationDeduplicationResult
        permits ObservationDeduplicationResult.Unique,
        ObservationDeduplicationResult.Duplicate {

    /**
     * Indicates whether the submitted fingerprint is already known.
     *
     * @return {@code true} for a duplicate result
     */
    boolean isDuplicate();

    default boolean isUnique() {
        return !isDuplicate();
    }

    /**
     * Indicates that no existing observation currently has the fingerprint.
     */
    record Unique()
            implements ObservationDeduplicationResult {

        @Override
        public boolean isDuplicate() {
            return false;
        }
    }

    /**
     * Indicates that an existing observation already owns the fingerprint.
     *
     * @param existingObservationId the matching observation
     */
    record Duplicate(
            ObservationId existingObservationId
    ) implements ObservationDeduplicationResult {

        public Duplicate {
            Objects.requireNonNull(
                    existingObservationId,
                    "existingObservationId must not be null"
            );
        }

        @Override
        public boolean isDuplicate() {
            return true;
        }
    }

    static Unique unique() {
        return new Unique();
    }

    static Duplicate duplicate(
            ObservationId existingObservationId
    ) {
        return new Duplicate(
                existingObservationId
        );
    }
}