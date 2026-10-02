package com.fold.modules.observations.domain.repository;

import com.fold.kernel.ids.ObservationId;
import com.fold.modules.observations.domain.model.Observation;
import com.fold.modules.observations.domain.value.ObservationFingerprint;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Test-only in-memory implementation of {@link ObservationRepository}.
 */
public final class InMemoryObservationRepository
        implements ObservationRepository {

    private final Map<ObservationId, Observation> byId =
            new HashMap<>();

    private final Map<ObservationFingerprint, ObservationId>
            byFingerprint =
            new HashMap<>();

    @Override
    public synchronized ObservationAddResult add(
            Observation observation
    ) {
        Objects.requireNonNull(
                observation,
                "observation must not be null"
        );

        Observation existingById =
                byId.get(
                        observation.id()
                );

        if (existingById != null) {
            if (existingById.fingerprint()
                    .equals(
                            observation.fingerprint()
                    )) {
                return ObservationAddResult.added();
            }

            throw new IllegalStateException(
                    "observation ID already exists with a different fingerprint"
            );
        }

        ObservationId existingOwner =
                byFingerprint.get(
                        observation.fingerprint()
                );

        if (existingOwner != null) {
            return ObservationAddResult.duplicate(
                    existingOwner
            );
        }

        byId.put(
                observation.id(),
                observation
        );

        byFingerprint.put(
                observation.fingerprint(),
                observation.id()
        );

        return ObservationAddResult.added();
    }

    @Override
    public synchronized Optional<Observation> findById(
            ObservationId id
    ) {
        Objects.requireNonNull(
                id,
                "id must not be null"
        );

        return Optional.ofNullable(
                byId.get(id)
        );
    }

    @Override
    public synchronized Optional<Observation> findByFingerprint(
            ObservationFingerprint fingerprint
    ) {
        Objects.requireNonNull(
                fingerprint,
                "fingerprint must not be null"
        );

        ObservationId id =
                byFingerprint.get(fingerprint);

        if (id == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(
                byId.get(id)
        );
    }

    public synchronized int size() {
        return byId.size();
    }

    public synchronized void clear() {
        byId.clear();
        byFingerprint.clear();
    }
}