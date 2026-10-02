package com.fold.modules.observations.infrastructure.web.response;

public record SubmitObservationResponse(
        String observationId,
        String outcome,
        boolean duplicate,
        Fingerprint fingerprint,
        Rejection rejection
) {

    public record Fingerprint(
            int version,
            String algorithm,
            String value
    ) {
    }

    public record Rejection(
            String code,
            String message
    ) {
    }
}