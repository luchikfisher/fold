package com.fold.modules.observations.infrastructure.web.response;

import java.math.BigDecimal;
import java.util.List;

public record ObservationResponse(
        String id,
        String type,
        Subject subject,
        List<Field> payload,
        Origin origin,
        Fingerprint fingerprint,
        String observedAt,
        String arrivedAt,
        String status,
        String decidedAt,
        Rejection rejection
) {

    public record Subject(
            String type,
            String externalKey
    ) {
    }

    public record Field(
            String name,
            Value value
    ) {
    }

    public record Value(
            String type,
            String text,
            BigDecimal number,
            Boolean booleanValue,
            String timestamp,
            String scheme,
            String identifier,
            List<Value> values
    ) {
    }

    public record Origin(
            String sourceId,
            String evidenceId,
            String externalRecordId
    ) {
    }

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