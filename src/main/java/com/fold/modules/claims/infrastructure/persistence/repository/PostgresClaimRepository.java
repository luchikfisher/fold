package com.fold.modules.claims.infrastructure.persistence.repository;

import com.fold.kernel.ids.ClaimId;
import com.fold.modules.claims.domain.model.Claim;
import com.fold.modules.claims.domain.repository.ClaimRepository;
import com.fold.modules.claims.infrastructure.persistence.mapper.ClaimPersistenceMapper;
import com.fold.modules.claims.infrastructure.persistence.record.ClaimOriginRecord;
import com.fold.modules.claims.infrastructure.persistence.record.ClaimRecord;
import com.fold.modules.claims.infrastructure.persistence.record.ClaimRevisionRecord;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Repository
public class PostgresClaimRepository
        implements ClaimRepository {

    private final NamedParameterJdbcTemplate jdbc;
    private final ClaimPersistenceMapper mapper;

    public PostgresClaimRepository(
            NamedParameterJdbcTemplate jdbc,
            ClaimPersistenceMapper mapper
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
    public void add(
            Claim claim
    ) {
        Objects.requireNonNull(
                claim,
                "claim must not be null"
        );

        ClaimRecord record =
                mapper.toRecord(claim);

        int inserted =
                jdbc.update(
                        """
                        INSERT INTO claims
                        (
                            id,
                            subject_namespace,
                            subject_value,
                            predicate,
                            object_payload,
                            derivation_kind,
                            derivation_method,
                            derivation_version,
                            confidence,
                            observed_at,
                            valid_from,
                            valid_to,
                            created_at,
                            status,
                            revision
                        )
                        VALUES
                        (
                            :id,
                            :subjectNamespace,
                            :subjectValue,
                            :predicate,
                            CAST(:objectPayload AS jsonb),
                            :derivationKind,
                            :derivationMethod,
                            :derivationVersion,
                            :confidence,
                            :observedAt,
                            :validFrom,
                            :validTo,
                            :createdAt,
                            :status,
                            :revision
                        )
                        """,
                        parameters(record)
                );

        if (inserted != 1) {
            throw new IllegalStateException(
                    "Claim insertion affected "
                            + inserted
                            + " rows"
            );
        }

        for (ClaimOriginRecord origin
                : mapper.toOriginRecords(claim)) {

            jdbc.update(
                    """
                    INSERT INTO claim_origin_references
                    (
                        claim_id,
                        origin_kind,
                        reference_id
                    )
                    VALUES
                    (
                        :claimId,
                        :kind,
                        :referenceId
                    )
                    """,
                    new MapSqlParameterSource()
                            .addValue(
                                    "claimId",
                                    origin.claimId()
                            )
                            .addValue(
                                    "kind",
                                    origin.kind()
                            )
                            .addValue(
                                    "referenceId",
                                    origin.referenceId()
                            )
            );
        }

        for (ClaimRevisionRecord revision
                : mapper.toRevisionRecords(claim)) {

            jdbc.update(
                    """
                    INSERT INTO claim_lifecycle_revisions
                    (
                        claim_id,
                        revision,
                        transition,
                        status,
                        changed_at,
                        related_claim_id,
                        reason_code,
                        reason_message
                    )
                    VALUES
                    (
                        :claimId,
                        :revision,
                        :transition,
                        :status,
                        :changedAt,
                        :relatedClaimId,
                        :reasonCode,
                        :reasonMessage
                    )
                    """,
                    revisionParameters(revision)
            );
        }
    }

    @Override
    public Optional<Claim> findById(
            ClaimId id
    ) {
        Objects.requireNonNull(
                id,
                "id must not be null"
        );

        List<ClaimRecord> claims =
                jdbc.query(
                        """
                        SELECT
                            id,
                            subject_namespace,
                            subject_value,
                            predicate,
                            object_payload::text AS object_payload,
                            derivation_kind,
                            derivation_method,
                            derivation_version,
                            confidence,
                            observed_at,
                            valid_from,
                            valid_to,
                            created_at,
                            status,
                            revision
                        FROM claims
                        WHERE id = :id
                        """,
                        new MapSqlParameterSource(
                                "id",
                                id.value()
                        ),
                        (rs, rowNumber) ->
                                new ClaimRecord(
                                        rs.getObject(
                                                "id",
                                                UUID.class
                                        ),
                                        rs.getString(
                                                "subject_namespace"
                                        ),
                                        rs.getString(
                                                "subject_value"
                                        ),
                                        rs.getString(
                                                "predicate"
                                        ),
                                        rs.getString(
                                                "object_payload"
                                        ),
                                        rs.getString(
                                                "derivation_kind"
                                        ),
                                        rs.getString(
                                                "derivation_method"
                                        ),
                                        rs.getInt(
                                                "derivation_version"
                                        ),
                                        (Double) rs.getObject(
                                                "confidence"
                                        ),
                                        instant(
                                                rs.getTimestamp(
                                                        "observed_at"
                                                )
                                        ),
                                        instant(
                                                rs.getTimestamp(
                                                        "valid_from"
                                                )
                                        ),
                                        instant(
                                                rs.getTimestamp(
                                                        "valid_to"
                                                )
                                        ),
                                        rs.getTimestamp(
                                                "created_at"
                                        ).toInstant(),
                                        rs.getString(
                                                "status"
                                        ),
                                        rs.getLong(
                                                "revision"
                                        )
                                )
                );

        if (claims.isEmpty()) {
            return Optional.empty();
        }

        List<ClaimOriginRecord> origins =
                jdbc.query(
                        """
                        SELECT
                            claim_id,
                            origin_kind,
                            reference_id
                        FROM claim_origin_references
                        WHERE claim_id = :claimId
                        ORDER BY origin_kind, reference_id
                        """,
                        new MapSqlParameterSource(
                                "claimId",
                                id.value()
                        ),
                        (rs, rowNumber) ->
                                new ClaimOriginRecord(
                                        rs.getObject(
                                                "claim_id",
                                                UUID.class
                                        ),
                                        rs.getString(
                                                "origin_kind"
                                        ),
                                        rs.getObject(
                                                "reference_id",
                                                UUID.class
                                        )
                                )
                );

        List<ClaimRevisionRecord> revisions =
                jdbc.query(
                        """
                        SELECT
                            claim_id,
                            revision,
                            transition,
                            status,
                            changed_at,
                            related_claim_id,
                            reason_code,
                            reason_message
                        FROM claim_lifecycle_revisions
                        WHERE claim_id = :claimId
                        ORDER BY revision
                        """,
                        new MapSqlParameterSource(
                                "claimId",
                                id.value()
                        ),
                        (rs, rowNumber) ->
                                new ClaimRevisionRecord(
                                        rs.getObject(
                                                "claim_id",
                                                UUID.class
                                        ),
                                        rs.getLong(
                                                "revision"
                                        ),
                                        rs.getString(
                                                "transition"
                                        ),
                                        rs.getString(
                                                "status"
                                        ),
                                        rs.getTimestamp(
                                                "changed_at"
                                        ).toInstant(),
                                        rs.getObject(
                                                "related_claim_id",
                                                UUID.class
                                        ),
                                        rs.getString(
                                                "reason_code"
                                        ),
                                        rs.getString(
                                                "reason_message"
                                        )
                                )
                );

        return Optional.of(
                mapper.toDomain(
                        claims.getFirst(),
                        origins,
                        revisions
                )
        );
    }

    private static MapSqlParameterSource parameters(
            ClaimRecord record
    ) {
        return new MapSqlParameterSource()
                .addValue(
                        "id",
                        record.id()
                )
                .addValue(
                        "subjectNamespace",
                        record.subjectNamespace()
                )
                .addValue(
                        "subjectValue",
                        record.subjectValue()
                )
                .addValue(
                        "predicate",
                        record.predicate()
                )
                .addValue(
                        "objectPayload",
                        record.objectPayload()
                )
                .addValue(
                        "derivationKind",
                        record.derivationKind()
                )
                .addValue(
                        "derivationMethod",
                        record.derivationMethod()
                )
                .addValue(
                        "derivationVersion",
                        record.derivationVersion()
                )
                .addValue(
                        "confidence",
                        record.confidence()
                )
                .addValue(
                        "observedAt",
                        timestamp(
                                record.observedAt()
                        )
                )
                .addValue(
                        "validFrom",
                        timestamp(
                                record.validFrom()
                        )
                )
                .addValue(
                        "validTo",
                        timestamp(
                                record.validTo()
                        )
                )
                .addValue(
                        "createdAt",
                        Timestamp.from(
                                record.createdAt()
                        )
                )
                .addValue(
                        "status",
                        record.status()
                )
                .addValue(
                        "revision",
                        record.revision()
                );
    }

    private static MapSqlParameterSource revisionParameters(
            ClaimRevisionRecord record
    ) {
        return new MapSqlParameterSource()
                .addValue(
                        "claimId",
                        record.claimId()
                )
                .addValue(
                        "revision",
                        record.revision()
                )
                .addValue(
                        "transition",
                        record.transition()
                )
                .addValue(
                        "status",
                        record.status()
                )
                .addValue(
                        "changedAt",
                        Timestamp.from(
                                record.changedAt()
                        )
                )
                .addValue(
                        "relatedClaimId",
                        record.relatedClaimId()
                )
                .addValue(
                        "reasonCode",
                        record.reasonCode()
                )
                .addValue(
                        "reasonMessage",
                        record.reasonMessage()
                );
    }

    private static Timestamp timestamp(
            java.time.Instant value
    ) {
        return value == null
                ? null
                : Timestamp.from(value);
    }

    private static java.time.Instant instant(
            Timestamp value
    ) {
        return value == null
                ? null
                : value.toInstant();
    }
}