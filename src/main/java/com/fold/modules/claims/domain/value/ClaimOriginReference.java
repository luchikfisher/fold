package com.fold.modules.claims.domain.value;

import com.fold.kernel.ids.ClaimId;
import com.fold.kernel.ids.EvidenceId;
import com.fold.kernel.ids.ObservationId;
import com.fold.kernel.ids.SourceId;

import java.util.Objects;

/**
 * Identifies an upstream artifact participating in the provenance of a claim.
 *
 * <p>Origin references preserve lineage without creating dependencies on the
 * internal implementation of the bounded context that owns the referenced
 * artifact.</p>
 *
 * <p>This type describes provenance references only. It does not describe the
 * derivation method used to produce the claim; that responsibility belongs to
 * {@link ClaimDerivation}.</p>
 */
public sealed interface ClaimOriginReference
        permits ClaimOriginReference.Observation,
        ClaimOriginReference.Source,
        ClaimOriginReference.Evidence,
        ClaimOriginReference.Claim {

    /**
     * References an authoritative Observation.
     *
     * @param observationId observation identity
     */
    record Observation(
            ObservationId observationId
    ) implements ClaimOriginReference {

        public Observation {
            Objects.requireNonNull(
                    observationId,
                    "observationId must not be null"
            );
        }
    }

    /**
     * References a Source.
     *
     * @param sourceId source identity
     */
    record Source(
            SourceId sourceId
    ) implements ClaimOriginReference {

        public Source {
            Objects.requireNonNull(
                    sourceId,
                    "sourceId must not be null"
            );
        }
    }

    /**
     * References durable Evidence.
     *
     * @param evidenceId evidence identity
     */
    record Evidence(
            EvidenceId evidenceId
    ) implements ClaimOriginReference {

        public Evidence {
            Objects.requireNonNull(
                    evidenceId,
                    "evidenceId must not be null"
            );
        }
    }

    /**
     * References another Claim used in producing this claim.
     *
     * <p>This supports future derived or inferred claims without making the
     * Claims vocabulary depend on a particular inference engine.</p>
     *
     * @param claimId upstream claim identity
     */
    record Claim(
            ClaimId claimId
    ) implements ClaimOriginReference {

        public Claim {
            Objects.requireNonNull(
                    claimId,
                    "claimId must not be null"
            );
        }
    }
}