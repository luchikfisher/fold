package com.fold.modules.observations.domain.policy;

import com.fold.modules.observations.domain.repository.ObservationRepository;
import com.fold.modules.observations.domain.value.ObservationDeduplicationResult;
import com.fold.modules.observations.domain.value.ObservationFingerprint;

import java.util.Objects;

/**
 * Determines whether a semantic observation has already been recorded.
 *
 * <p>Deduplication is based exclusively on
 * {@link ObservationFingerprint}. Aggregate IDs, arrival times, and transport
 * metadata do not participate in duplicate identity.</p>
 *
 * <p>This policy performs an advisory domain-level check. The persistence
 * implementation must additionally enforce fingerprint uniqueness because two
 * submissions may pass this check concurrently.</p>
 */
public final class ObservationDeduplicationPolicy {

    private final ObservationRepository repository;

    public ObservationDeduplicationPolicy(
            ObservationRepository repository
    ) {
        this.repository = Objects.requireNonNull(
                repository,
                "repository must not be null"
        );
    }

    /**
     * Checks whether the supplied fingerprint has already been recorded.
     *
     * @param fingerprint the semantic fingerprint
     * @return the deduplication outcome
     */
    public ObservationDeduplicationResult evaluate(
            ObservationFingerprint fingerprint
    ) {
        Objects.requireNonNull(
                fingerprint,
                "fingerprint must not be null"
        );

        return repository
                .findByFingerprint(fingerprint)
                .<ObservationDeduplicationResult>map(
                        observation ->
                                ObservationDeduplicationResult
                                        .duplicate(
                                                observation.id()
                                        )
                )
                .orElseGet(
                        ObservationDeduplicationResult::unique
                );
    }
}