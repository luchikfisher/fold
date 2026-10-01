package com.fold.modules.observations.api.view;

import com.fold.kernel.ids.EvidenceId;
import com.fold.kernel.ids.ObservationId;
import com.fold.kernel.ids.SourceId;
import com.fold.kernel.time.Timestamp;

import java.util.List;
import java.util.Objects;

/**
 * Public read representation of an authoritative observation.
 *
 * <p>The view exposes observation state without exposing the mutable aggregate
 * or any persistence representation.</p>
 *
 * @param id          observation identity
 * @param type        semantic observation type
 * @param subject     source-facing subject
 * @param payload     normalized semantic payload
 * @param origin      provenance references
 * @param fingerprint duplicate-detection fingerprint
 * @param observedAt  source-facing observation time
 * @param arrivedAt   FOLD arrival time
 * @param status      current lifecycle state
 * @param decidedAt   admission decision time, if decided
 * @param rejection   rejection reason, if rejected
 */
public record ObservationView(
        ObservationId id,
        String type,
        Subject subject,
        List<ObservationFieldView> payload,
        Origin origin,
        Fingerprint fingerprint,
        Timestamp observedAt,
        Timestamp arrivedAt,
        String status,
        Timestamp decidedAt,
        Rejection rejection
) {

    public ObservationView {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(subject, "subject must not be null");
        Objects.requireNonNull(payload, "payload must not be null");
        Objects.requireNonNull(origin, "origin must not be null");
        Objects.requireNonNull(
                fingerprint,
                "fingerprint must not be null"
        );
        Objects.requireNonNull(
                observedAt,
                "observedAt must not be null"
        );
        Objects.requireNonNull(
                arrivedAt,
                "arrivedAt must not be null"
        );
        Objects.requireNonNull(status, "status must not be null");

        payload = List.copyOf(payload);
    }

    public record Subject(
            String type,
            String externalKey
    ) {
        public Subject {
            Objects.requireNonNull(type, "type must not be null");
            Objects.requireNonNull(
                    externalKey,
                    "externalKey must not be null"
            );
        }
    }

    public record Origin(
            SourceId sourceId,
            EvidenceId evidenceId,
            String externalRecordId
    ) {
        public Origin {
            Objects.requireNonNull(
                    sourceId,
                    "sourceId must not be null"
            );
        }
    }

    public record Fingerprint(
            int version,
            String algorithm,
            String value
    ) {
        public Fingerprint {
            Objects.requireNonNull(
                    algorithm,
                    "algorithm must not be null"
            );
            Objects.requireNonNull(
                    value,
                    "value must not be null"
            );
        }
    }

    public record Rejection(
            String code,
            String message
    ) {
        public Rejection {
            Objects.requireNonNull(code, "code must not be null");
            Objects.requireNonNull(
                    message,
                    "message must not be null"
            );
        }
    }
}