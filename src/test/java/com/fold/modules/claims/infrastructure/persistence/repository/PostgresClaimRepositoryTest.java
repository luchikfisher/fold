package com.fold.modules.claims.infrastructure.persistence.repository;

import com.fold.kernel.ids.ClaimId;
import com.fold.kernel.time.Timestamp;
import com.fold.kernel.time.ValidTime;
import com.fold.modules.claims.domain.model.Claim;
import com.fold.modules.claims.domain.value.ClaimLifecycleReason;
import com.fold.modules.claims.domain.value.ClaimObject;
import com.fold.modules.claims.domain.value.ClaimObservedAt;
import com.fold.modules.claims.domain.value.ClaimProposition;
import com.fold.modules.claims.infrastructure.persistence.mapper.ClaimObjectJsonCodec;
import com.fold.modules.claims.infrastructure.persistence.mapper.ClaimPersistenceMapper;
import com.fold.testsupport.PostgresIntegrationTest;
import com.fold.testsupport.claims.ClaimTestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import tools.jackson.databind.ObjectMapper;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class PostgresClaimRepositoryTest
        extends PostgresIntegrationTest {

    @Autowired
    DataSource dataSource;

    @Autowired
    ObjectMapper objectMapper;

    private NamedParameterJdbcTemplate jdbc;
    private PostgresClaimRepository repository;

    @BeforeEach
    void setUp() {
        jdbc =
                new NamedParameterJdbcTemplate(
                        dataSource
                );

        repository =
                new PostgresClaimRepository(
                        jdbc,
                        new ClaimPersistenceMapper(
                                new ClaimObjectJsonCodec(
                                        objectMapper
                                )
                        )
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
    void shouldPersistAndRestoreCompleteClaim() {
        Claim original =
                ClaimTestFixtures.claim();

        repository.add(
                original
        );

        Claim restored =
                repository.findById(
                                original.id()
                        )
                        .orElseThrow();

        assertThat(restored.id())
                .isEqualTo(
                        original.id()
                );

        assertThat(restored.proposition())
                .isEqualTo(
                        original.proposition()
                );

        assertThat(restored.origin())
                .isEqualTo(
                        original.origin()
                );

        assertThat(restored.derivation())
                .isEqualTo(
                        original.derivation()
                );

        assertThat(restored.confidence())
                .isEqualTo(
                        original.confidence()
                );

        assertThat(restored.observedAt())
                .isEqualTo(
                        original.observedAt()
                );

        assertThat(restored.validTime())
                .isEqualTo(
                        original.validTime()
                );

        assertThat(restored.revisionHistory())
                .containsExactlyElementsOf(
                        original.revisionHistory()
                );
    }

    @Test
    void shouldRoundTripAllCanonicalTimesAndTimestampObjectExactly() {
        Timestamp start = Timestamp.parse("2026-10-03T10:00:00.123456789Z");
        Timestamp end = Timestamp.parse("2026-10-03T10:00:00.123457999Z");
        ClaimProposition template = ClaimTestFixtures.proposition();
        Claim claim = Claim.create(
                ClaimId.random(),
                new ClaimProposition(
                        template.subject(),
                        template.predicate(),
                        new ClaimObject.TimestampValue(start)
                ),
                ClaimTestFixtures.origin(),
                ClaimTestFixtures.derivation(),
                null,
                ClaimObservedAt.of(start),
                ValidTime.between(start, end),
                start
        ).claim();

        claim.invalidate(
                new ClaimLifecycleReason("RETRACTED", "Retracted by source"),
                Timestamp.parse("2026-10-03T10:00:00.123456999Z")
        );
        claim.reinstate(
                new ClaimLifecycleReason("RESTORED", "Retraction withdrawn"),
                end
        );

        repository.add(claim);
        Claim restored = repository.findById(claim.id()).orElseThrow();

        assertThat(restored.createdAt()).isEqualTo(claim.createdAt());
        assertThat(restored.observedAt()).isEqualTo(claim.observedAt());
        assertThat(restored.validTime()).isEqualTo(claim.validTime());
        assertThat(restored.proposition()).isEqualTo(claim.proposition());
        assertThat(restored.revisionHistory())
                .containsExactlyElementsOf(claim.revisionHistory());
    }

    @Test
    void unknownClaimShouldReturnEmpty() {
        assertThat(
                repository.findById(
                        ClaimId.random()
                )
        ).isEmpty();
    }

    @Test
    void shouldRoundTripLifecycleHistory() {
        Claim claim =
                ClaimTestFixtures.claim();

        claim.invalidate(
                new ClaimLifecycleReason(
                        "RETRACTED",
                        "Retracted by source"
                ),
                Timestamp.parse(
                        "2026-10-04T10:00:00Z"
                )
        );

        claim.reinstate(
                new ClaimLifecycleReason(
                        "RESTORED",
                        "Retraction withdrawn"
                ),
                Timestamp.parse(
                        "2026-10-05T10:00:00Z"
                )
        );

        repository.add(claim);

        Claim restored =
                repository.findById(
                                claim.id()
                        )
                        .orElseThrow();

        assertThat(restored.status())
                .isEqualTo(
                        claim.status()
                );

        assertThat(restored.revision())
                .isEqualTo(
                        claim.revision()
                );

        assertThat(restored.revisionHistory())
                .containsExactlyElementsOf(
                        claim.revisionHistory()
                );
    }

    @Test
    void databaseShouldRejectInvalidConfidence() {
        Claim claim =
                ClaimTestFixtures.claim();

        repository.add(claim);

        assertThatThrownBy(
                () -> jdbc.update(
                        """
                        UPDATE claims
                        SET confidence = 1.5
                        WHERE id = :id
                        """,
                        new org.springframework.jdbc.core.namedparam
                                .MapSqlParameterSource(
                                "id",
                                claim.id().value()
                        )
                )
        ).isInstanceOf(
                org.springframework.dao
                        .DataIntegrityViolationException.class
        );
    }

    @Test
    void databaseShouldRejectInvalidValidTime() {
        Claim claim =
                ClaimTestFixtures.claim();

        repository.add(claim);

        assertThatThrownBy(
                () -> jdbc.update(
                        """
                        UPDATE claims
                        SET
                            valid_from = TIMESTAMPTZ '2027-01-01 00:00:00+00',
                            valid_to = TIMESTAMPTZ '2026-01-01 00:00:00+00'
                        WHERE id = :id
                        """,
                        new org.springframework.jdbc.core.namedparam
                                .MapSqlParameterSource(
                                "id",
                                claim.id().value()
                        )
                )
        ).isInstanceOf(
                org.springframework.dao
                        .DataIntegrityViolationException.class
        );
    }
}
