package com.fold.modules.observations.application.query;

import com.fold.modules.observations.api.query.GetObservationQuery;
import com.fold.modules.observations.api.query.ObservationQueries;
import com.fold.modules.observations.api.view.ObservationView;
import com.fold.modules.observations.domain.repository.ObservationRepository;

import java.util.Objects;
import java.util.Optional;

/**
 * Retrieves one authoritative observation for read access.
 */
public final class GetObservationHandler
        implements ObservationQueries {

    private final ObservationRepository repository;

    public GetObservationHandler(
            ObservationRepository repository
    ) {
        this.repository = Objects.requireNonNull(
                repository,
                "repository must not be null"
        );
    }

    @Override
    public Optional<ObservationView> get(
            GetObservationQuery query
    ) {
        Objects.requireNonNull(
                query,
                "query must not be null"
        );

        return repository
                .findById(query.observationId())
                .map(ObservationViewMapper::map);
    }
}