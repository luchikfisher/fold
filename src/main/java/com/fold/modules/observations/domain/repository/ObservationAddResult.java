package com.fold.modules.observations.domain.repository;

import com.fold.kernel.ids.ObservationId;

import java.util.Objects;

/**
 * Represents the authoritative outcome of adding a new observation to the
 * observation store.
 *
 * <p>The result exists because duplicate detection performed before insertion
 * is inherently subject to races. Storage is the final authority on
 * fingerprint uniqueness.</p>
 */
public sealed interface ObservationAddResult
        permits ObservationAddResult.Added,
        ObservationAddResult.Duplicate {

    /**
     * Indicates whether the observation was added.
     *
     * @return {@code true} when persistence succeeded
     */
    boolean wasAdded();

    default boolean isDuplicate() {
        return !wasAdded();
    }

    /**
     * Indicates that the new observation was persisted successfully.
     */
    record Added()
            implements ObservationAddResult {

        @Override
        public boolean wasAdded() {
            return true;
        }
    }

    /**
     * Indicates that another observation already owns the same fingerprint.
     *
     * @param existingObservationId the authoritative existing observation
     */
    record Duplicate(
            ObservationId existingObservationId
    ) implements ObservationAddResult {

        public Duplicate {
            Objects.requireNonNull(
                    existingObservationId,
                    "existingObservationId must not be null"
            );
        }

        @Override
        public boolean wasAdded() {
            return false;
        }
    }

    static Added added() {
        return new Added();
    }

    static Duplicate duplicate(
            ObservationId existingObservationId
    ) {
        return new Duplicate(existingObservationId);
    }
}