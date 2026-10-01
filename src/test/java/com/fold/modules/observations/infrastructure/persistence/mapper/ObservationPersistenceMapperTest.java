package com.fold.modules.observations.infrastructure.persistence.mapper;

import com.fold.kernel.ids.EvidenceId;
import com.fold.kernel.ids.ObservationId;
import com.fold.kernel.ids.SourceId;
import com.fold.kernel.time.Timestamp;
import com.fold.modules.observations.domain.model.*;
import com.fold.modules.observations.domain.value.*;
import com.fold.modules.observations.infrastructure.persistence.record.ObservationRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;

class ObservationPersistenceMapperTest {

    private ObservationPersistenceMapper mapper;

    @BeforeEach
    void setUp() {
        mapper =
                new ObservationPersistenceMapper(
                        new ObservationPayloadJsonCodec(
                                new ObjectMapper()
                        )
                );
    }

    @Test
    void receivedObservationShouldRoundTrip() {
        Observation original =
                receivedObservation();

        Observation restored =
                mapper.toDomain(
                        mapper.toRecord(original)
                );

        assertEquivalent(
                original,
                restored
        );

        assertThat(restored.status())
                .isEqualTo(
                        ObservationStatus.RECEIVED
                );

        assertThat(restored.decidedAt())
                .isEmpty();

        assertThat(restored.rejection())
                .isEmpty();
    }

    @Test
    void acceptedObservationShouldRoundTrip() {
        Observation original =
                receivedObservation();

        Timestamp decidedAt =
                Timestamp.parse(
                        "2026-10-01T12:00:02Z"
                );

        original.accept(decidedAt);

        Observation restored =
                mapper.toDomain(
                        mapper.toRecord(original)
                );

        assertEquivalent(
                original,
                restored
        );

        assertThat(restored.status())
                .isEqualTo(
                        ObservationStatus.ACCEPTED
                );

        assertThat(restored.decidedAt())
                .contains(decidedAt);

        assertThat(restored.rejection())
                .isEmpty();
    }

    @Test
    void rejectedObservationShouldRoundTrip() {
        Observation original =
                receivedObservation();

        ObservationRejection rejection =
                ObservationRejection.of(
                        "unsupported-input",
                        "The input cannot enter the knowledge pipeline"
                );

        Timestamp decidedAt =
                Timestamp.parse(
                        "2026-10-01T12:00:02Z"
                );

        original.reject(
                rejection,
                decidedAt
        );

        Observation restored =
                mapper.toDomain(
                        mapper.toRecord(original)
                );

        assertEquivalent(
                original,
                restored
        );

        assertThat(restored.status())
                .isEqualTo(
                        ObservationStatus.REJECTED
                );

        assertThat(restored.decidedAt())
                .contains(decidedAt);

        assertThat(restored.rejection())
                .contains(rejection);
    }

    @Test
    void recordShouldContainExactPersistenceRepresentation() {
        Observation original =
                receivedObservation();

        ObservationRecord record =
                mapper.toRecord(original);

        assertThat(record.id())
                .isEqualTo(
                        original.id().value()
                );

        assertThat(record.type())
                .isEqualTo(
                        "organization-registration"
                );

        assertThat(record.subjectType())
                .isEqualTo("organization");

        assertThat(record.subjectExternalKey())
                .isEqualTo(
                        "registry:company:123"
                );

        assertThat(record.sourceId())
                .isEqualTo(
                        original.origin()
                                .sourceId()
                                .value()
                );

        assertThat(record.evidenceId())
                .isEqualTo(
                        original.origin()
                                .evidence()
                                .orElseThrow()
                                .value()
                );

        assertThat(record.fingerprintVersion())
                .isEqualTo(1);

        assertThat(record.fingerprintAlgorithm())
                .isEqualTo("SHA-256");
    }

    private static void assertEquivalent(
            Observation expected,
            Observation actual
    ) {
        assertThat(actual.id())
                .isEqualTo(expected.id());

        assertThat(actual.type())
                .isEqualTo(expected.type());

        assertThat(actual.subject())
                .isEqualTo(expected.subject());

        assertThat(actual.payload())
                .isEqualTo(expected.payload());

        assertThat(actual.origin())
                .isEqualTo(expected.origin());

        assertThat(actual.fingerprint())
                .isEqualTo(expected.fingerprint());

        assertThat(actual.observedAt())
                .isEqualTo(expected.observedAt());

        assertThat(actual.arrivedAt())
                .isEqualTo(expected.arrivedAt());

        assertThat(actual.status())
                .isEqualTo(expected.status());
    }

    private static Observation receivedObservation() {
        return Observation.receive(
                ObservationId.random(),
                ObservationType.of(
                        "organization-registration"
                ),
                ObservationSubject.of(
                        "organization",
                        "registry:company:123"
                ),
                ObservationPayload.of(
                        ObservationField.of(
                                "organization.name",
                                ObservationValue.Text.of(
                                        "Acme Ltd"
                                )
                        ),
                        ObservationField.of(
                                "organization.active",
                                ObservationValue.BooleanValue.of(
                                        true
                                )
                        )
                ),
                ObservationOrigin.fromRecordEvidence(
                        SourceId.random(),
                        EvidenceId.random(),
                        "record-123"
                ),
                ObservationFingerprint.sha256V1(
                        "0123456789abcdef".repeat(4)
                ),
                ObservedAt.of(
                        Timestamp.parse(
                                "2026-10-01T12:00:00Z"
                        )
                ),
                ArrivedAt.of(
                        Timestamp.parse(
                                "2026-10-01T12:00:01Z"
                        )
                )
        );
    }
}