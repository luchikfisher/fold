package com.fold.modules.claims.application.integration.observations;

import com.fold.modules.claims.application.feature.deriveclaims.ClaimDerivationBatch;
import com.fold.modules.claims.application.feature.deriveclaims.ClaimDerivationEngine;
import com.fold.modules.claims.domain.derivation.ClaimDerivationInput;
import com.fold.modules.observations.api.event.ObservationAcceptedV1;
import com.fold.modules.observations.api.query.GetObservationQuery;
import com.fold.modules.observations.api.query.ObservationQueries;
import com.fold.modules.observations.api.view.ObservationView;

import java.util.List;
import java.util.Objects;

/**
 * Prepares Claim derivation for an accepted authoritative Observation.
 *
 * <p>This handler performs no authoritative Claim creation, persistence,
 * idempotency reservation, or event publication. It resolves the accepted
 * Observation through its public API and produces deterministic derivation
 * batches to be committed transactionally by the authoritative Claim
 * processing flow.</p>
 */
public final class DeriveClaimsForAcceptedObservationHandler {

    private static final String ACCEPTED_STATUS =
            "ACCEPTED";

    private final ObservationQueries observationQueries;
    private final ObservationClaimDerivationInputMapper mapper;
    private final ClaimDerivationEngine derivationEngine;

    public DeriveClaimsForAcceptedObservationHandler(
            ObservationQueries observationQueries,
            ObservationClaimDerivationInputMapper mapper,
            ClaimDerivationEngine derivationEngine
    ) {
        this.observationQueries =
                Objects.requireNonNull(
                        observationQueries,
                        "observationQueries must not be null"
                );

        this.mapper =
                Objects.requireNonNull(
                        mapper,
                        "mapper must not be null"
                );

        this.derivationEngine =
                Objects.requireNonNull(
                        derivationEngine,
                        "derivationEngine must not be null"
                );
    }

    /**
     * Derives all applicable Claim batches for an accepted Observation.
     *
     * @throws IllegalStateException if the integration event references an
     *                               Observation that cannot be retrieved or is
     *                               not actually accepted
     */
    public List<ClaimDerivationBatch> handle(
            ObservationAcceptedV1 event
    ) {
        Objects.requireNonNull(
                event,
                "event must not be null"
        );

        ObservationView observation =
                observationQueries
                        .get(
                                new GetObservationQuery(
                                        event.observationId()
                                )
                        )
                        .orElseThrow(
                                () -> new IllegalStateException(
                                        "Accepted Observation could not be retrieved: "
                                                + event.observationId()
                                )
                        );

        if (!ACCEPTED_STATUS.equals(observation.status())) {
            throw new IllegalStateException(
                    "ObservationAcceptedV1 referenced an Observation "
                            + "whose authoritative status is "
                            + observation.status()
            );
        }

        ClaimDerivationInput input =
                mapper.map(observation);

        return derivationEngine.derive(input);
    }
}