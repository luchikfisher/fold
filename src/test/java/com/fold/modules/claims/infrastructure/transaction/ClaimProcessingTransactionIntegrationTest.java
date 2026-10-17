package com.fold.modules.claims.infrastructure.transaction;

import com.fold.kernel.events.EventEnvelope;
import com.fold.kernel.events.EventMetadata;
import com.fold.kernel.events.EventPublisher;
import com.fold.kernel.ids.ClaimId;
import com.fold.kernel.ids.EvidenceId;
import com.fold.kernel.time.FoldClock;
import com.fold.kernel.time.Timestamp;
import com.fold.modules.claims.application.feature.deriveclaims.ClaimDerivationEngine;
import com.fold.modules.claims.application.feature.deriveclaims.ProcessAcceptedObservationHandler;
import com.fold.modules.claims.application.integration.observations.AcceptedObservationClaimInputProvider;
import com.fold.modules.claims.application.integration.observations.ObservationClaimDerivationInputMapper;
import com.fold.modules.claims.domain.derivation.ClaimDerivationInput;
import com.fold.modules.claims.domain.derivation.ClaimDerivationRule;
import com.fold.modules.claims.domain.derivation.ClaimProposal;
import com.fold.modules.claims.domain.value.ClaimDerivation;
import com.fold.modules.claims.domain.value.ClaimDerivationKind;
import com.fold.modules.claims.domain.value.ClaimDerivationMethod;
import com.fold.modules.claims.domain.value.ClaimObject;
import com.fold.modules.claims.domain.value.ClaimOrigin;
import com.fold.modules.claims.domain.value.ClaimOriginReference;
import com.fold.modules.claims.domain.value.ClaimPredicate;
import com.fold.modules.claims.domain.value.ClaimProposition;
import com.fold.modules.claims.domain.value.ClaimSubject;
import com.fold.modules.claims.infrastructure.persistence.mapper.ClaimObjectJsonCodec;
import com.fold.modules.claims.infrastructure.persistence.mapper.ClaimPersistenceMapper;
import com.fold.modules.claims.infrastructure.persistence.repository.PostgresClaimProcessingRepository;
import com.fold.modules.claims.infrastructure.persistence.repository.PostgresClaimRepository;
import com.fold.modules.observations.api.event.ObservationAcceptedV1;
import com.fold.modules.observations.api.query.ObservationQueries;
import com.fold.modules.observations.api.view.ObservationView;
import com.fold.testsupport.PostgresIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.ObjectMapper;

import javax.sql.DataSource;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class ClaimProcessingTransactionIntegrationTest
        extends PostgresIntegrationTest {

    @Autowired
    DataSource dataSource;

    @Autowired
    PlatformTransactionManager transactionManager;

    @Autowired
    ObjectMapper objectMapper;

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
                        DELETE FROM integration_outbox;
                        DELETE FROM claim_processing;
                        DELETE FROM claim_lifecycle_revisions;
                        DELETE FROM claim_origin_references;
                        DELETE FROM claims;
                        """
                );
    }

    @Test
    void failureAfterClaimWriteMustRollBackEntireProcessingOperation() {
        ObservationView observation =
                observation();

        ProcessAcceptedObservationHandler handler =
                handler(
                        observation,
                        event -> {
                            throw new IllegalStateException(
                                    "synthetic publication failure"
                            );
                        }
                );

        TransactionTemplate transaction =
                new TransactionTemplate(
                        transactionManager
                );

        assertThatThrownBy(
                () -> transaction.executeWithoutResult(
                        status ->
                                handler.handle(
                                        envelope(
                                                observation
                                        )
                                )
                )
        )
                .isInstanceOf(
                        IllegalStateException.class
                )
                .hasMessage(
                        "synthetic publication failure"
                );

        assertThat(count("claims"))
                .isZero();

        assertThat(count(
                "claim_origin_references"
        )).isZero();

        assertThat(count(
                "claim_lifecycle_revisions"
        )).isZero();

        assertThat(count(
                "claim_processing"
        )).isZero();

        assertThat(count(
                "integration_outbox"
        )).isZero();
    }

    private ProcessAcceptedObservationHandler handler(
            ObservationView observation,
            EventPublisher publisher
    ) {
        return handler(observation, publisher, UnaryOperator.identity());
    }

    private ProcessAcceptedObservationHandler handler(
            ObservationView observation,
            EventPublisher publisher,
            UnaryOperator<ClaimOrigin> proposalOrigin
    ) {
        ObservationQueries queries =
                query ->
                        Optional.of(
                                observation
                        );

        ClaimDerivation derivation =
                new ClaimDerivation(
                        ClaimDerivationKind.OBSERVED,
                        new ClaimDerivationMethod(
                                "rollback-test",
                                1
                        )
                );

        ClaimDerivationRule rule =
                new ClaimDerivationRule() {

                    @Override
                    public ClaimDerivation derivation() {
                        return derivation;
                    }

                    @Override
                    public boolean supports(
                            ClaimDerivationInput input
                    ) {
                        return true;
                    }

                    @Override
                    public List<ClaimProposal> derive(
                            ClaimDerivationInput input
                    ) {
                        return List.of(
                                ClaimProposal.of(
                                        new ClaimProposition(
                                                ClaimSubject.of(
                                                        "registry",
                                                        "company-123"
                                                ),
                                                ClaimPredicate.of(
                                                        "country"
                                                ),
                                                new ClaimObject.Text(
                                                        "Germany"
                                                )
                                        ),
                                        proposalOrigin.apply(input.origin().toClaimOrigin())
                                )
                        );
                    }
                };

        ClaimPersistenceMapper mapper =
                new ClaimPersistenceMapper(
                        new ClaimObjectJsonCodec(
                                objectMapper
                        )
                );

        return new ProcessAcceptedObservationHandler(
                new AcceptedObservationClaimInputProvider(
                        queries,
                        new ObservationClaimDerivationInputMapper()
                ),
                new ClaimDerivationEngine(
                        List.of(rule)
                ),
                new PostgresClaimProcessingRepository(
                        jdbc
                ),
                new PostgresClaimRepository(
                        jdbc,
                        mapper
                ),
                fixedClock(),
                publisher
        );
    }

    private long count(
            String table
    ) {
        Long count =
                jdbc.getJdbcOperations()
                        .queryForObject(
                                "SELECT COUNT(*) FROM "
                                        + table,
                                Long.class
                        );

        return count == null
                ? 0
                : count;
    }

    private static FoldClock fixedClock() {
        return () ->
                Timestamp.parse(
                        "2026-10-03T12:00:00Z"
                );
    }

    private static EventEnvelope<ObservationAcceptedV1>
    envelope(
            ObservationView observation
    ) {
        Timestamp occurredAt =
                Timestamp.parse(
                        "2026-10-03T11:00:00Z"
                );

        return new EventEnvelope<>(
                EventMetadata.root(
                        occurredAt,
                        "observations"
                ),
                new ObservationAcceptedV1(
                        observation.id(),
                        occurredAt
                )
        );
    }

    private static ObservationView observation() {
        return observation(null);
    }

    private static ObservationView observation(EvidenceId evidenceId) {
        return new ObservationView(
                com.fold.kernel.ids
                        .ObservationId.random(),
                "company",
                new ObservationView.Subject(
                        "organization",
                        "company-123"
                ),
                List.of(),
                new ObservationView.Origin(
                        com.fold.kernel.ids
                                .SourceId.random(),
                        evidenceId,
                        null
                ),
                new ObservationView.Fingerprint(
                        1,
                        "SHA-256",
                        "a".repeat(64)
                ),
                Timestamp.parse(
                        "2026-10-03T09:00:00Z"
                ),
                Timestamp.parse(
                        "2026-10-03T09:01:00Z"
                ),
                "ACCEPTED",
                Timestamp.parse(
                        "2026-10-03T09:02:00Z"
                ),
                null
        );
    }

    @ParameterizedTest
    @ValueSource(classes = {ClaimOriginReference.Source.class, ClaimOriginReference.Evidence.class})
    void incompleteProvenanceShouldRollBackAcquisitionAndAllowCorrectedRetry(
            Class<? extends ClaimOriginReference> missingType
    ) {
        ObservationView observation = observation(EvidenceId.random());
        var outbox = new com.fold.platform.events.JdbcOutboxEventPublisher(jdbc,
                new com.fold.platform.events.OutboxEventSerializer(objectMapper));
        var invalidHandler = handler(observation, outbox, origin -> {
            var references = new LinkedHashSet<>(origin.references());
            references.removeIf(missingType::isInstance);
            return new ClaimOrigin(references);
        });
        var transaction = new TransactionTemplate(transactionManager);
        var event = envelope(observation);

        assertThatThrownBy(() -> transaction.executeWithoutResult(status -> invalidHandler.handle(event)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("required upstream provenance");
        for (String table : List.of("claims", "claim_origin_references", "claim_lifecycle_revisions",
                "claim_processing", "integration_outbox")) {
            assertThat(count(table)).as(table).isZero();
        }

        // The failed acquisition must not suppress a corrected retry, including extra provenance.
        var expected = new LinkedHashSet<>(new ObservationClaimDerivationInputMapper().map(observation)
                .origin().toClaimOrigin().references());
        expected.add(new ClaimOriginReference.Claim(ClaimId.random()));
        var correctedHandler = handler(observation, outbox, origin -> new ClaimOrigin(expected));
        transaction.executeWithoutResult(status -> correctedHandler.handle(event));
        assertThat(count("claims")).isEqualTo(1);
        assertThat(count("claim_processing")).isEqualTo(1);
        assertThat(count("integration_outbox")).isEqualTo(1);
        var id = new ClaimId(jdbc.getJdbcOperations().queryForObject("SELECT id FROM claims", java.util.UUID.class));
        var repository = new PostgresClaimRepository(jdbc,
                new ClaimPersistenceMapper(new ClaimObjectJsonCodec(objectMapper)));
        assertThat(repository.findById(id).orElseThrow().origin().references())
                .containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    void successfulProcessingShouldCommitClaimReceiptAndOutboxTogether() {
        ObservationView observation =
                observation();

        com.fold.platform.events.JdbcOutboxEventPublisher outbox =
                new com.fold.platform.events.JdbcOutboxEventPublisher(
                        jdbc,
                        new com.fold.platform.events.OutboxEventSerializer(
                                objectMapper
                        )
                );

        ProcessAcceptedObservationHandler handler =
                handler(
                        observation,
                        outbox
                );

        TransactionTemplate transaction =
                new TransactionTemplate(
                        transactionManager
                );

        transaction.executeWithoutResult(
                status ->
                        handler.handle(
                                envelope(
                                        observation
                                )
                        )
        );

        assertThat(count("claims"))
                .isEqualTo(1);

        assertThat(count("claim_processing"))
                .isEqualTo(1);

        assertThat(count(
                "claim_lifecycle_revisions"
        )).isEqualTo(1);

        Integer createdEvents =
                jdbc.getJdbcOperations()
                        .queryForObject(
                                """
                                SELECT COUNT(*)
                                FROM integration_outbox
                                WHERE event_type =
                                    'claims.claim-created'
                                """,
                                Integer.class
                        );

        assertThat(createdEvents)
                .isEqualTo(1);

        String state =
                jdbc.getJdbcOperations()
                        .queryForObject(
                                """
                                SELECT state
                                FROM claim_processing
                                """,
                                String.class
                        );

        assertThat(state)
                .isEqualTo(
                        "COMPLETED"
                );
    }
}
