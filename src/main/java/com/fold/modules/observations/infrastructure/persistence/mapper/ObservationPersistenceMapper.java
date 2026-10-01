package com.fold.modules.observations.infrastructure.persistence.mapper;

import com.fold.kernel.ids.EvidenceId;
import com.fold.kernel.ids.ObservationId;
import com.fold.kernel.ids.SourceId;
import com.fold.kernel.time.Timestamp;
import com.fold.modules.observations.domain.model.Observation;
import com.fold.modules.observations.domain.model.ObservationOrigin;
import com.fold.modules.observations.domain.model.ObservationSubject;
import com.fold.modules.observations.domain.value.*;
import com.fold.modules.observations.infrastructure.persistence.record.ObservationRecord;

import java.util.Objects;

public final class ObservationPersistenceMapper {

    private final ObservationPayloadJsonCodec payloadCodec;

    public ObservationPersistenceMapper(
            ObservationPayloadJsonCodec payloadCodec
    ) {
        this.payloadCodec = Objects.requireNonNull(
                payloadCodec,
                "payloadCodec must not be null"
        );
    }

    public ObservationRecord toRecord(
            Observation observation
    ) {
        Objects.requireNonNull(
                observation,
                "observation must not be null"
        );

        ObservationRejection rejection =
                observation.rejection()
                        .orElse(null);

        return new ObservationRecord(
                observation.id().value(),
                observation.type().value(),
                observation.subject().type(),
                observation.subject().externalKey(),
                observation.origin()
                        .sourceId()
                        .value(),
                observation.origin()
                        .evidence()
                        .map(EvidenceId::value)
                        .orElse(null),
                observation.origin()
                        .externalRecord()
                        .orElse(null),
                observation.fingerprint().version(),
                observation.fingerprint().algorithm(),
                observation.fingerprint().value(),
                observation.observedAt()
                        .value()
                        .value(),
                observation.arrivedAt()
                        .value()
                        .value(),
                observation.status().name(),
                observation.decidedAt()
                        .map(Timestamp::value)
                        .orElse(null),
                rejection == null
                        ? null
                        : rejection.code(),
                rejection == null
                        ? null
                        : rejection.message(),
                payloadCodec.encode(
                        observation.payload()
                )
        );
    }

    public Observation toDomain(
            ObservationRecord record
    ) {
        Objects.requireNonNull(
                record,
                "record must not be null"
        );

        ObservationOrigin origin =
                new ObservationOrigin(
                        new SourceId(record.sourceId()),
                        record.evidenceId() == null
                                ? null
                                : new EvidenceId(
                                record.evidenceId()
                        ),
                        record.externalRecordId()
                );

        ObservationStatus status =
                ObservationStatus.valueOf(
                        record.status()
                );

        ObservationRejection rejection =
                record.rejectionCode() == null
                        ? null
                        : new ObservationRejection(
                        record.rejectionCode(),
                        record.rejectionMessage()
                );

        return Observation.restore(
                new ObservationId(record.id()),
                new ObservationType(record.type()),
                new ObservationSubject(
                        record.subjectType(),
                        record.subjectExternalKey()
                ),
                payloadCodec.decode(
                        record.payload()
                ),
                origin,
                new ObservationFingerprint(
                        record.fingerprintVersion(),
                        record.fingerprintAlgorithm(),
                        record.fingerprintValue()
                ),
                ObservedAt.of(
                        Timestamp.of(
                                record.observedAt()
                        )
                ),
                ArrivedAt.of(
                        Timestamp.of(
                                record.arrivedAt()
                        )
                ),
                status,
                record.decidedAt() == null
                        ? null
                        : Timestamp.of(
                        record.decidedAt()
                ),
                rejection
        );
    }
}