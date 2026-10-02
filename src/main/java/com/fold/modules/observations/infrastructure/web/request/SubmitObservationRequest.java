package com.fold.modules.observations.infrastructure.web.request;

import java.util.List;

/**
 * HTTP request for submitting one normalized observation.
 */
public record SubmitObservationRequest(
        String type,
        Subject subject,
        List<Field> payload,
        Origin origin,
        String observedAt
) {

    public record Subject(
            String type,
            String externalKey
    ) {
    }

    public record Field(
            String name,
            ObservationValueRequest value
    ) {
    }

    public record Origin(
            String sourceId,
            String evidenceId,
            String externalRecordId
    ) {
    }
}