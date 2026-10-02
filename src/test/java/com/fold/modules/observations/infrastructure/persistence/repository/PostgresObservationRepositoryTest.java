package com.fold.modules.observations.infrastructure.persistence.repository;

import com.fold.kernel.ids.ObservationId;
import com.fold.kernel.ids.SourceId;
import com.fold.kernel.time.Timestamp;
import com.fold.modules.observations.domain.model.*;
import com.fold.modules.observations.domain.repository.ObservationAddResult;
import com.fold.modules.observations.domain.value.*;
import com.fold.modules.observations.infrastructure.persistence.mapper.ObservationPayloadJsonCodec;
import com.fold.modules.observations.infrastructure.persistence.mapper.ObservationPersistenceMapper;
import com.fold.testsupport.PostgresIntegrationTest;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import tools.jackson.databind.ObjectMapper;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;

class PostgresObservationRepositoryTest
        extends PostgresIntegrationTest {

    private PostgresObservationRepository repository;

    @BeforeEach
    void setUp() {
        DataSource dataSource =
                new DriverManagerDataSource(
                        POSTGRES.getJdbcUrl(),
                        POSTGRES.getUsername(),
                        POSTGRES.getPassword()
                );

        Flyway.configure()
                .dataSource(dataSource)
                .cleanDisabled(false)
                .load()
                .clean();

        Flyway.configure()
                .dataSource(dataSource)
                .load()
                .migrate();

        ObservationPayloadJsonCodec codec =
                new ObservationPayloadJsonCodec(
                        new ObjectMapper()
                );

        repository =
                new PostgresObservationRepository(
                        new NamedParameterJdbcTemplate(
                                dataSource
                        ),
                        new ObservationPersistenceMapper(
                                codec
                        )
                );
    }

    @Test
    void shouldPersistAndRestoreAcceptedObservation() {
        Observation observation =
                observation();

        observation.accept(
                Timestamp.parse(
                        "2026-09-29T10:00:02Z"
                )
        );

        ObservationAddResult result =
                repository.add(observation);

        assertThat(result.wasAdded()).isTrue();

        Observation restored =
                repository.findById(
                                observation.id()
                        )
                        .orElseThrow();

        assertThat(restored.id())
                .isEqualTo(observation.id());

        assertThat(restored.type())
                .isEqualTo(observation.type());

        assertThat(restored.subject())
                .isEqualTo(observation.subject());

        assertThat(restored.payload())
                .isEqualTo(observation.payload());

        assertThat(restored.origin())
                .isEqualTo(observation.origin());

        assertThat(restored.fingerprint())
                .isEqualTo(
                        observation.fingerprint()
                );

        assertThat(restored.status())
                .isEqualTo(
                        ObservationStatus.ACCEPTED
                );
    }

    @Test
    void fingerprintShouldBeAuthoritativelyUnique() {
        Observation first =
                observation();

        first.accept(
                Timestamp.parse(
                        "2026-09-29T10:00:02Z"
                )
        );

        repository.add(first);

        Observation second =
                Observation.receive(
                        ObservationId.random(),
                        first.type(),
                        first.subject(),
                        first.payload(),
                        first.origin(),
                        first.fingerprint(),
                        first.observedAt(),
                        first.arrivedAt()
                );

        second.accept(
                Timestamp.parse(
                        "2026-09-29T10:00:03Z"
                )
        );

        ObservationAddResult result =
                repository.add(second);

        assertThat(result.isDuplicate())
                .isTrue();

        ObservationAddResult.Duplicate duplicate =
                (ObservationAddResult.Duplicate) result;

        assertThat(
                duplicate.existingObservationId()
        ).isEqualTo(
                first.id()
        );
    }

    @Test
    void shouldFindObservationByFingerprint() {
        Observation observation =
                observation();

        observation.accept(
                Timestamp.parse(
                        "2026-09-29T10:00:02Z"
                )
        );

        repository.add(observation);

        assertThat(
                repository.findByFingerprint(
                        observation.fingerprint()
                )
        ).contains(observation);
    }

    private static Observation observation() {
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
                ObservationOrigin.fromRecord(
                        SourceId.random(),
                        "record-123"
                ),
                ObservationFingerprint.sha256V1(
                        "0123456789abcdef".repeat(4)
                ),
                ObservedAt.of(
                        Timestamp.parse(
                                "2026-09-29T10:00:00Z"
                        )
                ),
                ArrivedAt.of(
                        Timestamp.parse(
                                "2026-09-29T10:00:01Z"
                        )
                )
        );
    }

    @Test
    void observedAtShouldRoundTripWithoutPrecisionDrift() {
        Observation observation =
                Observation.receive(
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
                                )
                        ),
                        ObservationOrigin.fromRecord(
                                SourceId.random(),
                                "record-123"
                        ),
                        ObservationFingerprint.sha256V1(
                                "0123456789abcdef".repeat(4)
                        ),
                        ObservedAt.of(
                                Timestamp.parse(
                                        "2026-09-29T10:00:00.123456789Z"
                                )
                        ),
                        ArrivedAt.of(
                                Timestamp.parse(
                                        "2026-09-29T10:00:01Z"
                                )
                        )
                );

        observation.accept(
                Timestamp.parse(
                        "2026-09-29T10:00:02Z"
                )
        );

        repository.add(observation);

        Observation restored =
                repository.findById(
                        observation.id()
                ).orElseThrow();

        assertThat(restored.observedAt())
                .isEqualTo(
                        observation.observedAt()
                );

        assertThat(restored.observedAt().value())
                .isEqualTo(
                        Timestamp.parse(
                                "2026-09-29T10:00:00.123456Z"
                        )
                );
    }
}