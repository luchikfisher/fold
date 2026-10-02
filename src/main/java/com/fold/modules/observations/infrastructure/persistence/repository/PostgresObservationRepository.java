package com.fold.modules.observations.infrastructure.persistence.repository;

import com.fold.kernel.ids.ObservationId;
import com.fold.modules.observations.domain.model.Observation;
import com.fold.modules.observations.domain.repository.ObservationAddResult;
import com.fold.modules.observations.domain.repository.ObservationRepository;
import com.fold.modules.observations.domain.value.ObservationFingerprint;
import com.fold.modules.observations.infrastructure.persistence.mapper.ObservationPersistenceMapper;
import com.fold.modules.observations.infrastructure.persistence.record.ObservationRecord;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Repository
public class PostgresObservationRepository
        implements ObservationRepository {

    private final NamedParameterJdbcTemplate jdbc;
    private final ObservationPersistenceMapper mapper;

    public PostgresObservationRepository(
            NamedParameterJdbcTemplate jdbc,
            ObservationPersistenceMapper mapper
    ) {
        this.jdbc = Objects.requireNonNull(
                jdbc,
                "jdbc must not be null"
        );
        this.mapper = Objects.requireNonNull(
                mapper,
                "mapper must not be null"
        );
    }

    @Override
    public ObservationAddResult add(
            Observation observation
    ) {
        Objects.requireNonNull(
                observation,
                "observation must not be null"
        );

        ObservationRecord record =
                mapper.toRecord(observation);

        String sql = """
                INSERT INTO observations
                (
                    id,
                    type,
                    subject_type,
                    subject_external_key,
                    source_id,
                    evidence_id,
                    external_record_id,
                    fingerprint_version,
                    fingerprint_algorithm,
                    fingerprint_value,
                    observed_at,
                    arrived_at,
                    status,
                    decided_at,
                    rejection_code,
                    rejection_message,
                    payload
                )
                VALUES
                (
                    :id,
                    :type,
                    :subjectType,
                    :subjectExternalKey,
                    :sourceId,
                    :evidenceId,
                    :externalRecordId,
                    :fingerprintVersion,
                    :fingerprintAlgorithm,
                    :fingerprintValue,
                    :observedAt,
                    :arrivedAt,
                    :status,
                    :decidedAt,
                    :rejectionCode,
                    :rejectionMessage,
                    CAST(:payload AS jsonb)
                )
                ON CONFLICT
                (
                    fingerprint_version,
                    fingerprint_algorithm,
                    fingerprint_value
                )
                DO NOTHING
                RETURNING id
                """;

        List<UUID> inserted =
                jdbc.query(
                        sql,
                        parameters(record),
                        (resultSet, rowNumber) ->
                                resultSet.getObject(
                                        "id",
                                        UUID.class
                                )
                );

        if (!inserted.isEmpty()) {
            return ObservationAddResult.added();
        }

        ObservationId existing =
                findIdByFingerprint(
                        observation.fingerprint()
                )
                        .orElseThrow(
                                () -> new IllegalStateException(
                                        "Fingerprint conflict occurred but the owning observation could not be found"
                                )
                        );

        return ObservationAddResult.duplicate(
                existing
        );
    }

    @Override
    public Optional<Observation> findById(
            ObservationId id
    ) {
        Objects.requireNonNull(
                id,
                "id must not be null"
        );

        String sql = """
                SELECT
                    id,
                    type,
                    subject_type,
                    subject_external_key,
                    source_id,
                    evidence_id,
                    external_record_id,
                    fingerprint_version,
                    fingerprint_algorithm,
                    fingerprint_value,
                    observed_at,
                    arrived_at,
                    status,
                    decided_at,
                    rejection_code,
                    rejection_message,
                    payload::text AS payload
                FROM observations
                WHERE id = :id
                """;

        List<ObservationRecord> records =
                jdbc.query(
                        sql,
                        new MapSqlParameterSource(
                                "id",
                                id.value()
                        ),
                        ROW_MAPPER
                );

        return records.stream()
                .findFirst()
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Observation> findByFingerprint(
            ObservationFingerprint fingerprint
    ) {
        Objects.requireNonNull(
                fingerprint,
                "fingerprint must not be null"
        );

        String sql = """
                SELECT
                    id,
                    type,
                    subject_type,
                    subject_external_key,
                    source_id,
                    evidence_id,
                    external_record_id,
                    fingerprint_version,
                    fingerprint_algorithm,
                    fingerprint_value,
                    observed_at,
                    arrived_at,
                    status,
                    decided_at,
                    rejection_code,
                    rejection_message,
                    payload::text AS payload
                FROM observations
                WHERE fingerprint_version = :version
                  AND fingerprint_algorithm = :algorithm
                  AND fingerprint_value = :value
                """;

        List<ObservationRecord> records =
                jdbc.query(
                        sql,
                        fingerprintParameters(
                                fingerprint
                        ),
                        ROW_MAPPER
                );

        return records.stream()
                .findFirst()
                .map(mapper::toDomain);
    }

    private Optional<ObservationId> findIdByFingerprint(
            ObservationFingerprint fingerprint
    ) {
        String sql = """
                SELECT id
                FROM observations
                WHERE fingerprint_version = :version
                  AND fingerprint_algorithm = :algorithm
                  AND fingerprint_value = :value
                """;

        List<ObservationId> ids =
                jdbc.query(
                        sql,
                        fingerprintParameters(
                                fingerprint
                        ),
                        (resultSet, rowNumber) ->
                                new ObservationId(
                                        resultSet.getObject(
                                                "id",
                                                UUID.class
                                        )
                                )
                );

        return ids.stream().findFirst();
    }

    private static MapSqlParameterSource parameters(
            ObservationRecord record
    ) {
        return new MapSqlParameterSource()
                .addValue("id", record.id())
                .addValue("type", record.type())
                .addValue(
                        "subjectType",
                        record.subjectType()
                )
                .addValue(
                        "subjectExternalKey",
                        record.subjectExternalKey()
                )
                .addValue(
                        "sourceId",
                        record.sourceId()
                )
                .addValue(
                        "evidenceId",
                        record.evidenceId()
                )
                .addValue(
                        "externalRecordId",
                        record.externalRecordId()
                )
                .addValue(
                        "fingerprintVersion",
                        record.fingerprintVersion()
                )
                .addValue(
                        "fingerprintAlgorithm",
                        record.fingerprintAlgorithm()
                )
                .addValue(
                        "fingerprintValue",
                        record.fingerprintValue()
                )
                .addValue(
                        "observedAt",
                        Timestamp.from(
                                record.observedAt()
                        )
                )
                .addValue(
                        "arrivedAt",
                        Timestamp.from(
                                record.arrivedAt()
                        )
                )
                .addValue(
                        "status",
                        record.status()
                )
                .addValue(
                        "decidedAt",
                        record.decidedAt() == null
                                ? null
                                : Timestamp.from(
                                record.decidedAt()
                        )
                )
                .addValue(
                        "rejectionCode",
                        record.rejectionCode()
                )
                .addValue(
                        "rejectionMessage",
                        record.rejectionMessage()
                )
                .addValue(
                        "payload",
                        record.payload()
                );
    }

    private static MapSqlParameterSource fingerprintParameters(
            ObservationFingerprint fingerprint
    ) {
        return new MapSqlParameterSource()
                .addValue(
                        "version",
                        fingerprint.version()
                )
                .addValue(
                        "algorithm",
                        fingerprint.algorithm()
                )
                .addValue(
                        "value",
                        fingerprint.value()
                );
    }

    private static final RowMapper<ObservationRecord>
            ROW_MAPPER =
            new RowMapper<>() {

                @Override
                public ObservationRecord mapRow(
                        ResultSet resultSet,
                        int rowNumber
                ) throws SQLException {

                    Timestamp decidedAt =
                            resultSet.getTimestamp(
                                    "decided_at"
                            );

                    return new ObservationRecord(
                            resultSet.getObject(
                                    "id",
                                    UUID.class
                            ),
                            resultSet.getString("type"),
                            resultSet.getString(
                                    "subject_type"
                            ),
                            resultSet.getString(
                                    "subject_external_key"
                            ),
                            resultSet.getObject(
                                    "source_id",
                                    UUID.class
                            ),
                            resultSet.getObject(
                                    "evidence_id",
                                    UUID.class
                            ),
                            resultSet.getString(
                                    "external_record_id"
                            ),
                            resultSet.getInt(
                                    "fingerprint_version"
                            ),
                            resultSet.getString(
                                    "fingerprint_algorithm"
                            ),
                            resultSet.getString(
                                    "fingerprint_value"
                            ),
                            resultSet.getTimestamp(
                                    "observed_at"
                            ).toInstant(),
                            resultSet.getTimestamp(
                                    "arrived_at"
                            ).toInstant(),
                            resultSet.getString(
                                    "status"
                            ),
                            decidedAt == null
                                    ? null
                                    : decidedAt.toInstant(),
                            resultSet.getString(
                                    "rejection_code"
                            ),
                            resultSet.getString(
                                    "rejection_message"
                            ),
                            resultSet.getString(
                                    "payload"
                            )
                    );
                }
            };
}