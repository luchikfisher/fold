package com.fold.modules.claims.infrastructure.persistence.repository;

import com.fold.kernel.ids.ClaimId;
import com.fold.kernel.ids.ObservationId;
import com.fold.kernel.time.Timestamp;
import com.fold.modules.claims.application.idempotency.ClaimProcessingAcquireResult;
import com.fold.modules.claims.application.idempotency.ClaimProcessingKey;
import com.fold.modules.claims.application.idempotency.ClaimProcessingReceipt;
import com.fold.modules.claims.domain.value.ClaimDerivation;
import com.fold.modules.claims.domain.value.ClaimDerivationKind;
import com.fold.modules.claims.domain.value.ClaimDerivationMethod;
import com.fold.testsupport.PostgresIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class PostgresClaimProcessingRepositoryTest
        extends PostgresIntegrationTest {

    @Autowired
    DataSource dataSource;

    private NamedParameterJdbcTemplate jdbc;
    private PostgresClaimProcessingRepository repository;

    @BeforeEach
    void setUp() {
        jdbc =
                new NamedParameterJdbcTemplate(
                        dataSource
                );

        repository =
                new PostgresClaimProcessingRepository(
                        jdbc
                );

        jdbc.getJdbcOperations()
                .execute(
                        """
                        DELETE FROM claim_processing;
                        """
                );
    }

    @Test
    void firstAcquisitionShouldSucceed() {
        ClaimProcessingAcquireResult result =
                repository.acquire(
                        key()
                );

        assertThat(result)
                .isInstanceOf(
                        ClaimProcessingAcquireResult
                                .Acquired.class
                );

        Integer count =
                jdbc.getJdbcOperations()
                        .queryForObject(
                                """
                                SELECT COUNT(*)
                                FROM claim_processing
                                """,
                                Integer.class
                        );

        assertThat(count)
                .isEqualTo(1);
    }

    @Test
    void completedProcessingShouldReturnExistingReceipt() {
        ClaimProcessingKey key =
                key();

        repository.acquire(key);

        ClaimId first =
                ClaimId.random();

        ClaimId second =
                ClaimId.random();

        ClaimProcessingReceipt completed =
                new ClaimProcessingReceipt(
                        key,
                        Timestamp.parse(
                                "2026-10-03T12:00:00Z"
                        ),
                        List.of(
                                first,
                                second
                        )
                );

        repository.complete(
                completed
        );

        ClaimProcessingAcquireResult result =
                repository.acquire(key);

        assertThat(result)
                .isInstanceOf(
                        ClaimProcessingAcquireResult
                                .AlreadyCompleted.class
                );

        ClaimProcessingReceipt restored =
                ((ClaimProcessingAcquireResult.AlreadyCompleted)
                        result)
                        .receipt();

        assertThat(restored.key())
                .isEqualTo(key);

        assertThat(restored.producedClaimIds())
                .containsExactly(
                        first,
                        second
                );
    }

    @Test
    void zeroOutputCompletionShouldRemainDurable() {
        ClaimProcessingKey key =
                key();

        repository.acquire(key);

        repository.complete(
                new ClaimProcessingReceipt(
                        key,
                        Timestamp.parse(
                                "2026-10-03T12:00:00Z"
                        ),
                        List.of()
                )
        );

        ClaimProcessingAcquireResult result =
                repository.acquire(key);

        ClaimProcessingReceipt receipt =
                ((ClaimProcessingAcquireResult.AlreadyCompleted)
                        result)
                        .receipt();

        assertThat(receipt.producedClaimIds())
                .isEmpty();
    }

    @Test
    void operationCannotBeCompletedTwice() {
        ClaimProcessingKey key =
                key();

        repository.acquire(key);

        ClaimProcessingReceipt receipt =
                new ClaimProcessingReceipt(
                        key,
                        Timestamp.parse(
                                "2026-10-03T12:00:00Z"
                        ),
                        List.of()
                );

        repository.complete(receipt);

        assertThatThrownBy(
                () -> repository.complete(
                        receipt
                )
        )
                .isInstanceOf(
                        IllegalStateException.class
                )
                .hasMessageContaining(
                        "updated 0"
                );
    }

    @Test
    void differentRuleVersionShouldHaveDifferentProcessingIdentity() {
        ObservationId observationId =
                ObservationId.random();

        ClaimProcessingKey versionOne =
                key(
                        observationId,
                        1
                );

        ClaimProcessingKey versionTwo =
                key(
                        observationId,
                        2
                );

        assertThat(
                repository.acquire(versionOne)
        ).isInstanceOf(
                ClaimProcessingAcquireResult
                        .Acquired.class
        );

        assertThat(
                repository.acquire(versionTwo)
        ).isInstanceOf(
                ClaimProcessingAcquireResult
                        .Acquired.class
        );

        Integer count =
                jdbc.getJdbcOperations()
                        .queryForObject(
                                """
                                SELECT COUNT(*)
                                FROM claim_processing
                                """,
                                Integer.class
                        );

        assertThat(count)
                .isEqualTo(2);
    }

    private static ClaimProcessingKey key() {
        return key(
                ObservationId.random(),
                1
        );
    }

    private static ClaimProcessingKey key(
            ObservationId observationId,
            int version
    ) {
        return new ClaimProcessingKey(
                observationId,
                new ClaimDerivation(
                        ClaimDerivationKind.OBSERVED,
                        new ClaimDerivationMethod(
                                "test-rule",
                                version
                        )
                )
        );
    }
}