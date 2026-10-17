package com.fold.modules.claims.infrastructure.persistence;

import com.fold.testsupport.PostgresIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class ClaimsMigrationConstraintsTest
        extends PostgresIntegrationTest {

    @Autowired
    DataSource dataSource;

    private NamedParameterJdbcTemplate jdbc;

    @BeforeEach
    void setUp() {
        jdbc =
                new NamedParameterJdbcTemplate(
                        dataSource
                );

        jdbc.getJdbcOperations()
                .execute(
                        """
                        DELETE FROM claim_processing;
                        DELETE FROM claim_lifecycle_revisions;
                        DELETE FROM claim_origin_references;
                        DELETE FROM claims;
                        """
                );
    }

    @Test
    void claimRevisionMustBePositive() {
        assertThatThrownBy(
                () -> insertClaim(
                        0,
                        "ACTIVE"
                )
        ).isInstanceOf(
                DataIntegrityViolationException.class
        );
    }

    @Test
    void unknownClaimStatusMustBeRejected() {
        assertThatThrownBy(
                () -> insertClaim(
                        1,
                        "UNKNOWN"
                )
        ).isInstanceOf(
                DataIntegrityViolationException.class
        );
    }

    @Test
    void invalidConfidenceMustBeRejected() {
        UUID id =
                UUID.randomUUID();

        assertThatThrownBy(
                () -> jdbc.update(
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
                            created_at,
                            status,
                            revision
                        )
                        VALUES
                        (
                            :id,
                            'registry',
                            '123',
                            'country',
                            '{"type":"text","value":"Germany"}'::jsonb,
                            'OBSERVED',
                            'test',
                            1,
                            1.1,
                            CURRENT_TIMESTAMP,
                            'ACTIVE',
                            1
                        )
                        """,
                        new MapSqlParameterSource(
                                "id",
                                id
                        )
                )
        ).isInstanceOf(
                DataIntegrityViolationException.class
        );
    }

    @Test
    void pendingProcessingMustNotContainCompletionData() {
        assertThatThrownBy(
                () -> jdbc.update(
                        """
                        INSERT INTO claim_processing
                        (
                            observation_id,
                            derivation_kind,
                            derivation_method,
                            derivation_version,
                            state,
                            processed_at,
                            produced_claim_ids
                        )
                        VALUES
                        (
                            :observationId,
                            'OBSERVED',
                            'test',
                            1,
                            'PENDING',
                            CURRENT_TIMESTAMP,
                            ARRAY[]::uuid[]
                        )
                        """,
                        new MapSqlParameterSource(
                                "observationId",
                                UUID.randomUUID()
                        )
                )
        ).isInstanceOf(
                DataIntegrityViolationException.class
        );
    }

    @Test
    void completedProcessingMustContainReceiptData() {
        assertThatThrownBy(
                () -> jdbc.update(
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
                            'OBSERVED',
                            'test',
                            1,
                            'COMPLETED'
                        )
                        """,
                        new MapSqlParameterSource(
                                "observationId",
                                UUID.randomUUID()
                        )
                )
        ).isInstanceOf(
                DataIntegrityViolationException.class
        );
    }

    private void insertClaim(
            long revision,
            String status
    ) {
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
                    created_at,
                    status,
                    revision
                )
                VALUES
                (
                    :id,
                    'registry',
                    '123',
                    'country',
                    '{"type":"text","value":"Germany"}'::jsonb,
                    'OBSERVED',
                    'test',
                    1,
                    CURRENT_TIMESTAMP,
                    :status,
                    :revision
                )
                """,
                new MapSqlParameterSource()
                        .addValue(
                                "id",
                                UUID.randomUUID()
                        )
                        .addValue(
                                "status",
                                status
                        )
                        .addValue(
                                "revision",
                                revision
                        )
        );
    }
}