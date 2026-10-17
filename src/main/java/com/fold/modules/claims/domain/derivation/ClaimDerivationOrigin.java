package com.fold.modules.claims.domain.derivation;

import com.fold.kernel.ids.EvidenceId;
import com.fold.kernel.ids.ObservationId;
import com.fold.kernel.ids.SourceId;
import com.fold.modules.claims.domain.value.ClaimOrigin;
import com.fold.modules.claims.domain.value.ClaimOriginReference;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Represents upstream provenance available to an Observation-based Claim
 * derivation.
 *
 * <p>The originating Observation is mandatory. Source and Evidence references
 * are preserved when available.</p>
 *
 * <p>The external record identifier is retained as derivation input even
 * though the current ClaimOrigin vocabulary does not duplicate that value.
 * The authoritative Observation already preserves it.</p>
 *
 * @param observationId   authoritative upstream Observation
 * @param sourceId        authoritative Source reference
 * @param evidenceId      optional Evidence reference
 * @param externalRecordId optional upstream record identifier
 */
public record ClaimDerivationOrigin(
        ObservationId observationId,
        SourceId sourceId,
        EvidenceId evidenceId,
        String externalRecordId
) {

    public ClaimDerivationOrigin {
        Objects.requireNonNull(
                observationId,
                "observationId must not be null"
        );

        Objects.requireNonNull(
                sourceId,
                "sourceId must not be null"
        );
    }

    public Optional<EvidenceId> evidence() {
        return Optional.ofNullable(evidenceId);
    }

    public Optional<String> externalRecord() {
        return Optional.ofNullable(externalRecordId);
    }

    /**
     * Builds the minimum Claim provenance that must accompany Claims produced
     * from this Observation.
     *
     * <p>The returned origin always references the originating Observation and
     * Source and includes Evidence when available.</p>
     */
    public ClaimOrigin toClaimOrigin() {
        Set<ClaimOriginReference> references =
                new LinkedHashSet<>();

        references.add(
                new ClaimOriginReference.Observation(
                        observationId
                )
        );

        references.add(
                new ClaimOriginReference.Source(
                        sourceId
                )
        );

        if (evidenceId != null) {
            references.add(
                    new ClaimOriginReference.Evidence(
                            evidenceId
                    )
            );
        }

        return new ClaimOrigin(references);
    }
}