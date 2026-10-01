package com.fold.modules.observations.domain.model;

import com.fold.kernel.ids.EvidenceId;
import com.fold.kernel.ids.SourceId;

import java.util.Objects;
import java.util.Optional;

/**
 * Identifies where an observation originated.
 *
 * <p>Every observation belongs to a known source. When addressable evidence
 * exists, the origin may additionally reference that evidence. An external
 * record identifier may be retained when the source exposes a stable identity
 * for the original record.</p>
 *
 * <p>Origin describes provenance references only. Source metadata and evidence
 * content remain owned by their respective bounded contexts.</p>
 *
 * @param sourceId         the source that supplied the observation
 * @param evidenceId       addressable evidence associated with the observation, if any
 * @param externalRecordId the source's record identifier, if any
 */
public record ObservationOrigin(
        SourceId sourceId,
        EvidenceId evidenceId,
        String externalRecordId
) {

    public ObservationOrigin {
        Objects.requireNonNull(
                sourceId,
                "sourceId must not be null"
        );

        if (externalRecordId != null) {
            if (externalRecordId.isBlank()) {
                throw new IllegalArgumentException(
                        "externalRecordId must not be blank when present"
                );
            }

            if (!externalRecordId.equals(externalRecordId.trim())) {
                throw new IllegalArgumentException(
                        "externalRecordId must not contain leading or trailing whitespace"
                );
            }
        }
    }

    public static ObservationOrigin fromSource(
            SourceId sourceId
    ) {
        return new ObservationOrigin(
                sourceId,
                null,
                null
        );
    }

    public static ObservationOrigin fromEvidence(
            SourceId sourceId,
            EvidenceId evidenceId
    ) {
        Objects.requireNonNull(
                evidenceId,
                "evidenceId must not be null"
        );

        return new ObservationOrigin(
                sourceId,
                evidenceId,
                null
        );
    }

    public static ObservationOrigin fromRecord(
            SourceId sourceId,
            String externalRecordId
    ) {
        return new ObservationOrigin(
                sourceId,
                null,
                externalRecordId
        );
    }

    public static ObservationOrigin fromRecordEvidence(
            SourceId sourceId,
            EvidenceId evidenceId,
            String externalRecordId
    ) {
        Objects.requireNonNull(
                evidenceId,
                "evidenceId must not be null"
        );

        return new ObservationOrigin(
                sourceId,
                evidenceId,
                externalRecordId
        );
    }

    public Optional<EvidenceId> evidence() {
        return Optional.ofNullable(evidenceId);
    }

    public Optional<String> externalRecord() {
        return Optional.ofNullable(externalRecordId);
    }
}