package com.fold.modules.claims.application.integration.observations;

import com.fold.modules.claims.domain.derivation.ClaimDerivationInput;
import com.fold.modules.observations.api.event.ObservationAcceptedV1;
import com.fold.modules.observations.api.query.GetObservationQuery;
import com.fold.modules.observations.api.query.ObservationQueries;
import com.fold.modules.observations.api.view.ObservationView;

import java.util.Objects;

/**
 * Resolves an accepted Observation through the public Observations API and
 * translates it into Claims-owned derivation input.
 */
public final class AcceptedObservationClaimInputProvider {

    private static final String ACCEPTED =
            "ACCEPTED";

    private final ObservationQueries queries;
    private final ObservationClaimDerivationInputMapper mapper;

    public AcceptedObservationClaimInputProvider(
            ObservationQueries queries,
            ObservationClaimDerivationInputMapper mapper
    ) {
        this.queries = Objects.requireNonNull(
                queries,
                "queries must not be null"
        );

        this.mapper = Objects.requireNonNull(
                mapper,
                "mapper must not be null"
        );
    }

    public ClaimDerivationInput load(
            ObservationAcceptedV1 event
    ) {
        Objects.requireNonNull(
                event,
                "event must not be null"
        );

        ObservationView observation =
                queries.get(
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

        if (!ACCEPTED.equals(
                observation.status()
        )) {
            throw new IllegalStateException(
                    "ObservationAcceptedV1 referenced Observation "
                            + observation.id()
                            + " whose authoritative status is "
                            + observation.status()
            );
        }

        return mapper.map(observation);
    }
}