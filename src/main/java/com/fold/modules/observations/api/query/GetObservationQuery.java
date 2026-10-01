package com.fold.modules.observations.api.query;

import com.fold.kernel.ids.ObservationId;

import java.util.Objects;

/**
 * Requests one observation by its authoritative identity.
 *
 * @param observationId the requested observation
 */
public record GetObservationQuery(
        ObservationId observationId
) {

    public GetObservationQuery {
        Objects.requireNonNull(
                observationId,
                "observationId must not be null"
        );
    }
}