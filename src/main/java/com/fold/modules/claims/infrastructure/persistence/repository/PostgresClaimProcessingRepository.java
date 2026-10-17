package com.fold.modules.claims.infrastructure.persistence.repository;

import com.fold.kernel.ids.ClaimId;
import com.fold.kernel.ids.ObservationId;
import com.fold.kernel.time.Timestamp;
import com.fold.modules.claims.application.idempotency.*;
import com.fold.modules.claims.domain.value.ClaimDerivation;
import com.fold.modules.claims.domain.value.ClaimDerivationKind;
import com.fold.modules.claims.domain.value.ClaimDerivationMethod;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Array;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Repository
public class PostgresClaimProcessingRepository
        implements ClaimProcessingRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public PostgresClaimProcessingRepository(
            NamedParameterJdbcTemplate jdbc
    ) {
        this.jdbc = Objects.requireNonNull(
                jdbc,
                "jdbc must not be null"
        );
    }

    @Override
    public ClaimProcessingAcquireResult acquire(
            ClaimProcessingKey key
    ) {
        Objects.requireNonNull(
                key,
                "key must not be null"
        );

        List<UUID> inserted =
                jdbc.query(
                        """
                        INSERT INTO claim_processing
                        (
                            observation_id,
                            derivation_kind,
                            derivation_method,
                            derivation_version,
                            state
                        )
                        VALUES
                        (
                            :observationId,
                            :kind,
                            :method,
                            :version,
                            'PENDING'
                        )
                        ON CONFLICT
                        (
                            observation_id,
                            derivation_kind,
                            derivation_method,
                            derivation_version
                        )
                        DO NOTHING
                        RETURNING observation_id
                        """,
                        keyParameters(key),
                        (rs, rowNumber) ->
                                rs.getObject(
                                        "observation_id",
                                        UUID.class
                                )
                );

        if (!inserted.isEmpty()) {
            return new ClaimProcessingAcquireResult.Acquired();
        }

        return loadExisting(key);
    }

    @Override
    public void complete(
            ClaimProcessingReceipt receipt
    ) {
        Objects.requireNonNull(
                receipt,
                "receipt must not be null"
        );

        int updated =
                jdbc.update(
                        """
                        UPDATE claim_processing
                        SET
                            state = 'COMPLETED',
                            processed_at = :processedAt,
                            produced_claim_ids =
                                CAST(:producedClaimIds AS uuid[])
                        WHERE observation_id = :observationId
                          AND derivation_kind = :kind
                          AND derivation_method = :method
                          AND derivation_version = :version
                          AND state = 'PENDING'
                        """,
                        keyParameters(
                                receipt.key()
                        )
                                .addValue(
                                        "processedAt",
                                        java.sql.Timestamp.from(
                                                receipt.processedAt()
                                                        .value()
                                        )
                                )
                                .addValue(
                                        "producedClaimIds",
                                        postgresUuidArrayLiteral(
                                                receipt.producedClaimIds()
                                        )
                                )
                );

        if (updated != 1) {
            throw new IllegalStateException(
                    "Claim processing completion expected one PENDING row "
                            + "but updated "
                            + updated
            );
        }
    }

    private ClaimProcessingAcquireResult loadExisting(
            ClaimProcessingKey key
    ) {
        List<StoredProcessing> existing =
                jdbc.query(
                        """
                        SELECT
                            state,
                            processed_at,
                            produced_claim_ids
                        FROM claim_processing
                        WHERE observation_id = :observationId
                          AND derivation_kind = :kind
                          AND derivation_method = :method
                          AND derivation_version = :version
                        """,
                        keyParameters(key),
                        (rs, rowNumber) ->
                                new StoredProcessing(
                                        rs.getString(
                                                "state"
                                        ),
                                        rs.getTimestamp(
                                                "processed_at"
                                        ),
                                        rs.getArray(
                                                "produced_claim_ids"
                                        )
                                )
                );

        if (existing.isEmpty()) {
            throw new IllegalStateException(
                    "Claim processing conflict occurred but the existing "
                            + "processing row could not be found"
            );
        }

        StoredProcessing stored =
                existing.getFirst();

        if (!"COMPLETED".equals(stored.state())) {
            throw new IllegalStateException(
                    "Existing Claim processing operation is not COMPLETED: "
                            + stored.state()
            );
        }

        if (stored.processedAt() == null
                || stored.claimIds() == null) {
            throw new IllegalStateException(
                    "Completed Claim processing row is incomplete"
            );
        }

        return new ClaimProcessingAcquireResult
                .AlreadyCompleted(
                new ClaimProcessingReceipt(
                        key,
                        Timestamp.of(
                                stored.processedAt()
                                        .toInstant()
                        ),
                        readClaimIds(
                                stored.claimIds()
                        )
                )
        );
    }

    private static MapSqlParameterSource keyParameters(
            ClaimProcessingKey key
    ) {
        ClaimDerivation derivation =
                key.derivation();

        return new MapSqlParameterSource()
                .addValue(
                        "observationId",
                        key.observationId()
                                .value()
                )
                .addValue(
                        "kind",
                        derivation.kind()
                                .name()
                )
                .addValue(
                        "method",
                        derivation.method()
                                .name()
                )
                .addValue(
                        "version",
                        derivation.method()
                                .version()
                );
    }

    private static String postgresUuidArrayLiteral(
            List<ClaimId> ids
    ) {
        if (ids.isEmpty()) {
            return "{}";
        }

        return ids.stream()
                .map(id ->
                        id.value().toString()
                )
                .collect(
                        java.util.stream.Collectors
                                .joining(
                                        ",",
                                        "{",
                                        "}"
                                )
                );
    }

    private static List<ClaimId> readClaimIds(
            Array array
    ) {
        try {
            Object raw =
                    array.getArray();

            if (!(raw instanceof Object[] values)) {
                throw new IllegalStateException(
                        "Unexpected PostgreSQL UUID array representation"
                );
            }

            return Arrays.stream(values)
                    .map(value -> {
                        if (value instanceof UUID uuid) {
                            return new ClaimId(uuid);
                        }

                        return new ClaimId(
                                UUID.fromString(
                                        value.toString()
                                )
                        );
                    })
                    .toList();

        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Failed to read Claim processing output IDs",
                    exception
            );
        }
    }

    private record StoredProcessing(
            String state,
            java.sql.Timestamp processedAt,
            Array claimIds
    ) {
    }
}