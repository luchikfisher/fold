package com.fold.modules.observations.api.query;

import com.fold.modules.observations.api.view.ObservationView;

import java.util.Optional;

/**
 * Public read boundary for authoritative Observation data.
 *
 * <p>Other bounded contexts may use this interface to retrieve public
 * Observation representations without depending on Observations application,
 * domain, repository, persistence, or infrastructure classes.</p>
 */
public interface ObservationQueries {

    /**
     * Retrieves one authoritative Observation.
     *
     * @param query requested Observation
     * @return public Observation view, if present
     */
    Optional<ObservationView> get(
            GetObservationQuery query
    );
}