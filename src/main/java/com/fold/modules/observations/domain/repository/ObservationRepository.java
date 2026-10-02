package com.fold.modules.observations.domain.repository;

import com.fold.kernel.ids.ObservationId;
import com.fold.modules.observations.domain.model.Observation;
import com.fold.modules.observations.domain.value.ObservationFingerprint;

import java.util.Optional;

/**
 * Provides authoritative persistence access to observations.
 *
 * <p>The repository contract is owned by the observations domain. It describes
 * only storage behavior required by the domain and contains no persistence
 * technology concepts.</p>
 *
 * <p>Fingerprint uniqueness is authoritative at insertion time. A preliminary
 * duplicate lookup is useful for avoiding unnecessary work, but
 * {@link #add(Observation)} must still detect a concurrent insertion of the
 * same fingerprint.</p>
 */
public interface ObservationRepository {

    /**
     * Adds a new observation to the authoritative store.
     *
     * <p>The operation must be atomic with respect to fingerprint uniqueness.
     * If another observation already owns the same complete fingerprint
     * identity, the existing observation is returned as a duplicate result
     * instead of creating a second observation.</p>
     *
     * @param observation the new observation
     * @return the authoritative insertion outcome
     */
    ObservationAddResult add(
            Observation observation
    );

    /**
     * Finds an observation by aggregate identity.
     *
     * @param id the observation identity
     * @return the observation, if present
     */
    Optional<Observation> findById(
            ObservationId id
    );

    /**
     * Finds an observation by semantic fingerprint.
     *
     * @param fingerprint the fingerprint
     * @return the matching observation, if present
     */
    Optional<Observation> findByFingerprint(
            ObservationFingerprint fingerprint
    );

    /**
     * Tests whether an observation with the supplied fingerprint already
     * exists.
     *
     * @param fingerprint the fingerprint
     * @return {@code true} when a matching observation exists
     */
    default boolean existsByFingerprint(
            ObservationFingerprint fingerprint
    ) {
        return findByFingerprint(
                fingerprint
        ).isPresent();
    }
}