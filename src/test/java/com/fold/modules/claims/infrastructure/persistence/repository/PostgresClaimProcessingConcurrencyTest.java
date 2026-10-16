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
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class PostgresClaimProcessingConcurrencyTest
        extends PostgresIntegrationTest {

    @Autowired
    DataSource dataSource;

    @Autowired
    PlatformTransactionManager transactionManager;

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
                        """
                );
    }

    @Test
    void concurrentAcquisitionShouldHaveSingleOwner()
            throws Exception {

        ClaimProcessingKey key =
                key();

        ClaimId producedClaim =
                ClaimId.random();

        CountDownLatch acquired =
                new CountDownLatch(1);

        CountDownLatch release =
                new CountDownLatch(1);

        CompletableFuture<ClaimProcessingAcquireResult>
                first =
                CompletableFuture.supplyAsync(
                        () -> {
                            TransactionTemplate tx =
                                    new TransactionTemplate(
                                            transactionManager
                                    );

                            return tx.execute(
                                    status -> {
                                        PostgresClaimProcessingRepository repository =
                                                new PostgresClaimProcessingRepository(
                                                        jdbc
                                                );

                                        ClaimProcessingAcquireResult result =
                                                repository.acquire(
                                                        key
                                                );

                                        acquired.countDown();

                                        try {
                                            release.await(
                                                    5,
                                                    TimeUnit.SECONDS
                                            );
                                        } catch (InterruptedException exception) {
                                            Thread.currentThread()
                                                    .interrupt();

                                            throw new IllegalStateException(
                                                    exception
                                            );
                                        }

                                        repository.complete(
                                                new ClaimProcessingReceipt(
                                                        key,
                                                        Timestamp.parse(
                                                                "2026-10-03T12:00:00Z"
                                                        ),
                                                        List.of(
                                                                producedClaim
                                                        )
                                                )
                                        );

                                        return result;
                                    }
                            );
                        }
                );

        assertThat(
                acquired.await(
                        5,
                        TimeUnit.SECONDS
                )
        ).isTrue();

        CompletableFuture<ClaimProcessingAcquireResult>
                second =
                CompletableFuture.supplyAsync(
                        () -> {
                            TransactionTemplate tx =
                                    new TransactionTemplate(
                                            transactionManager
                                    );

                            return tx.execute(
                                    status ->
                                            new PostgresClaimProcessingRepository(
                                                    jdbc
                                            ).acquire(key)
                            );
                        }
                );

        /*
         * Give the second transaction time to reach the unique-key conflict.
         * It should wait for the first transaction rather than acquire a
         * second processing row.
         */
        Thread.sleep(150);

        release.countDown();

        ClaimProcessingAcquireResult firstResult =
                first.get(
                        5,
                        TimeUnit.SECONDS
                );

        ClaimProcessingAcquireResult secondResult =
                second.get(
                        5,
                        TimeUnit.SECONDS
                );

        assertThat(firstResult)
                .isInstanceOf(
                        ClaimProcessingAcquireResult
                                .Acquired.class
                );

        assertThat(secondResult)
                .isInstanceOf(
                        ClaimProcessingAcquireResult
                                .AlreadyCompleted.class
                );

        var receipt =
                ((ClaimProcessingAcquireResult.AlreadyCompleted)
                        secondResult)
                        .receipt();

        assertThat(receipt.producedClaimIds())
                .containsExactly(
                        producedClaim
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

    private static ClaimProcessingKey key() {
        return new ClaimProcessingKey(
                ObservationId.random(),
                new ClaimDerivation(
                        ClaimDerivationKind.OBSERVED,
                        new ClaimDerivationMethod(
                                "concurrency-test",
                                1
                        )
                )
        );
    }
}