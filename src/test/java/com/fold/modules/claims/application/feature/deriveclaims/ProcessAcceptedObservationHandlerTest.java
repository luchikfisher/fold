package com.fold.modules.claims.application.feature.deriveclaims;

import com.fold.kernel.events.EventEnvelope;
import com.fold.kernel.events.EventMetadata;
import com.fold.kernel.events.EventPublisher;
import com.fold.kernel.ids.ClaimId;
import com.fold.kernel.ids.ObservationId;
import com.fold.kernel.ids.SourceId;
import com.fold.kernel.time.FoldClock;
import com.fold.kernel.time.Timestamp;
import com.fold.modules.claims.api.event.ClaimCreatedV1;
import com.fold.modules.claims.application.idempotency.ClaimProcessingAcquireResult;
import com.fold.modules.claims.application.idempotency.ClaimProcessingKey;
import com.fold.modules.claims.application.idempotency.ClaimProcessingReceipt;
import com.fold.modules.claims.application.idempotency.ClaimProcessingRepository;
import com.fold.modules.claims.application.integration.observations.AcceptedObservationClaimInputProvider;
import com.fold.modules.claims.application.integration.observations.ObservationClaimDerivationInputMapper;
import com.fold.modules.claims.domain.derivation.ClaimDerivationInput;
import com.fold.modules.claims.domain.derivation.ClaimDerivationRule;
import com.fold.modules.claims.domain.derivation.ClaimProposal;
import com.fold.modules.claims.domain.model.Claim;
import com.fold.modules.claims.domain.repository.ClaimRepository;
import com.fold.modules.claims.domain.value.ClaimDerivation;
import com.fold.modules.claims.domain.value.ClaimDerivationKind;
import com.fold.modules.claims.domain.value.ClaimDerivationMethod;
import com.fold.modules.claims.domain.value.ClaimObject;
import com.fold.modules.claims.domain.value.ClaimOrigin;
import com.fold.modules.claims.domain.value.ClaimPredicate;
import com.fold.modules.claims.domain.value.ClaimProposition;
import com.fold.modules.claims.domain.value.ClaimSubject;
import com.fold.modules.observations.api.event.ObservationAcceptedV1;
import com.fold.modules.observations.api.query.ObservationQueries;
import com.fold.modules.observations.api.view.ObservationView;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class ProcessAcceptedObservationHandlerTest {

    private static final Timestamp T0 =
            Timestamp.parse(
                    "2026-10-03T10:00:00Z"
            );

    private static final Timestamp T1 =
            Timestamp.parse(
                    "2026-10-03T10:00:01Z"
            );

    private static final Timestamp T2 =
            Timestamp.parse(
                    "2026-10-03T10:00:02Z"
            );

    @Test
    void newlyAcquiredProcessingShouldCreatePersistAndPublishClaim() {
        Fixture fixture =
                fixture(
                        AcquireMode.ACQUIRED,
                        true
                );

        ProcessAcceptedObservationResult result =
                fixture.handler.handle(
                        fixture.envelope
                );

        assertThat(
                fixture.claimRepository.claims
        ).hasSize(1);

        Claim claim =
                fixture.claimRepository
                        .claims
                        .getFirst();

        assertThat(claim.derivation())
                .isEqualTo(
                        fixture.derivation
                );

        assertThat(
                fixture.eventPublisher.events
        ).hasSize(1);

        EventEnvelope<?> published =
                fixture.eventPublisher
                        .events
                        .getFirst();

        assertThat(published.payload())
                .isInstanceOf(
                        ClaimCreatedV1.class
                );

        assertThat(
                published.metadata()
                        .correlationId()
        ).isEqualTo(
                fixture.envelope
                        .metadata()
                        .correlationId()
        );

        assertThat(
                published.metadata()
                        .causation()
        ).contains(
                fixture.envelope
                        .metadata()
                        .eventId()
        );

        assertThat(
                fixture.processingRepository
                        .completed
        ).hasSize(1);

        assertThat(result.completedNowCount())
                .isEqualTo(1);
    }

    @Test
    void alreadyCompletedProcessingMustNotExecuteDerivationAgain() {
        Fixture fixture =
                fixture(
                        AcquireMode.ALREADY_COMPLETED,
                        true
                );

        fixture.handler.handle(
                fixture.envelope
        );

        assertThat(
                fixture.derivationExecutions.get()
        ).isZero();

        assertThat(
                fixture.claimRepository.claims
        ).isEmpty();

        assertThat(
                fixture.eventPublisher.events
        ).isEmpty();

        assertThat(
                fixture.processingRepository.completed
        ).isEmpty();
    }

    @Test
    void zeroClaimDerivationShouldStillCompleteProcessing() {
        Fixture fixture =
                fixture(
                        AcquireMode.ACQUIRED,
                        false
                );

        ProcessAcceptedObservationResult result =
                fixture.handler.handle(
                        fixture.envelope
                );

        assertThat(
                fixture.claimRepository.claims
        ).isEmpty();

        assertThat(
                fixture.eventPublisher.events
        ).isEmpty();

        assertThat(
                fixture.processingRepository.completed
        ).hasSize(1);

        assertThat(
                fixture.processingRepository
                        .completed
                        .getFirst()
                        .producedClaimIds()
        ).isEmpty();

        assertThat(result.completedNowCount())
                .isEqualTo(1);
    }

    @Test
    void multipleProposalsShouldCreateOneClaimPerProposal() {
        Fixture fixture =
                fixture(
                        AcquireMode.ACQUIRED,
                        true
                );

        fixture.rule.proposalCount = 3;

        fixture.handler.handle(
                fixture.envelope
        );

        assertThat(
                fixture.claimRepository.claims
        ).hasSize(3);

        assertThat(
                fixture.eventPublisher.events
        ).hasSize(3);

        assertThat(
                fixture.processingRepository
                        .completed
                        .getFirst()
                        .producedClaimIds()
        ).hasSize(3);
    }

    private static Fixture fixture(
            AcquireMode mode,
            boolean produceClaim
    ) {
        ObservationView observation =
                observation();

        ObservationQueries queries =
                query ->
                        Optional.of(
                                observation
                        );

        AcceptedObservationClaimInputProvider inputProvider =
                new AcceptedObservationClaimInputProvider(
                        queries,
                        new ObservationClaimDerivationInputMapper()
                );

        ClaimDerivation derivation =
                new ClaimDerivation(
                        ClaimDerivationKind.OBSERVED,
                        new ClaimDerivationMethod(
                                "test-rule",
                                1
                        )
                );

        AtomicInteger executions =
                new AtomicInteger();

        MutableRule rule =
                new MutableRule(
                        derivation,
                        executions,
                        produceClaim ? 1 : 0
                );

        ClaimDerivationEngine engine =
                new ClaimDerivationEngine(
                        List.of(rule)
                );

        RecordingProcessingRepository processingRepository =
                new RecordingProcessingRepository(
                        mode,
                        derivation,
                        observation.id()
                );

        RecordingClaimRepository claimRepository =
                new RecordingClaimRepository();

        RecordingEventPublisher publisher =
                new RecordingEventPublisher();

        FoldClock clock =
                new SequenceClock(
                        List.of(
                                T1,
                                T2,
                                Timestamp.parse(
                                        "2026-10-03T10:00:03Z"
                                ),
                                Timestamp.parse(
                                        "2026-10-03T10:00:04Z"
                                ),
                                Timestamp.parse(
                                        "2026-10-03T10:00:05Z"
                                ),
                                Timestamp.parse(
                                        "2026-10-03T10:00:06Z"
                                ),
                                Timestamp.parse(
                                        "2026-10-03T10:00:07Z"
                                ),
                                Timestamp.parse(
                                        "2026-10-03T10:00:08Z"
                                )
                        )
                );

        EventEnvelope<ObservationAcceptedV1> envelope =
                new EventEnvelope<>(
                        EventMetadata.root(
                                T0,
                                "observations"
                        ),
                        new ObservationAcceptedV1(
                                observation.id(),
                                T0
                        )
                );

        ProcessAcceptedObservationHandler handler =
                new ProcessAcceptedObservationHandler(
                        inputProvider,
                        engine,
                        processingRepository,
                        claimRepository,
                        clock,
                        publisher
                );

        return new Fixture(
                handler,
                envelope,
                derivation,
                executions,
                rule,
                processingRepository,
                claimRepository,
                publisher
        );
    }

    private static ObservationView observation() {
        return new ObservationView(
                ObservationId.random(),
                "test-type",
                new ObservationView.Subject(
                        "organization",
                        "company-123"
                ),
                List.of(),
                new ObservationView.Origin(
                        SourceId.random(),
                        null,
                        null
                ),
                new ObservationView.Fingerprint(
                        1,
                        "SHA-256",
                        "a".repeat(64)
                ),
                Timestamp.parse(
                        "2026-10-02T10:00:00Z"
                ),
                Timestamp.parse(
                        "2026-10-02T10:01:00Z"
                ),
                "ACCEPTED",
                Timestamp.parse(
                        "2026-10-02T10:01:01Z"
                ),
                null
        );
    }

    private enum AcquireMode {
        ACQUIRED,
        ALREADY_COMPLETED
    }

    private static final class MutableRule
            implements ClaimDerivationRule {

        private final ClaimDerivation derivation;
        private final AtomicInteger executions;
        private int proposalCount;

        private MutableRule(
                ClaimDerivation derivation,
                AtomicInteger executions,
                int proposalCount
        ) {
            this.derivation = derivation;
            this.executions = executions;
            this.proposalCount = proposalCount;
        }

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
            executions.incrementAndGet();

            List<ClaimProposal> proposals =
                    new ArrayList<>();

            for (int index = 0;
                 index < proposalCount;
                 index++) {

                proposals.add(
                        ClaimProposal.of(
                                new ClaimProposition(
                                        ClaimSubject.of(
                                                "registry",
                                                "company-123"
                                        ),
                                        ClaimPredicate.of(
                                                "test-predicate-"
                                                        + index
                                        ),
                                        new ClaimObject.Text(
                                                "value-"
                                                        + index
                                        )
                                ),
                                input.origin()
                                        .toClaimOrigin()
                        )
                );
            }

            return List.copyOf(proposals);
        }
    }

    private static final class RecordingProcessingRepository
            implements ClaimProcessingRepository {

        private final AcquireMode mode;
        private final ClaimDerivation derivation;
        private final ObservationId observationId;
        private final List<ClaimProcessingReceipt> completed =
                new ArrayList<>();

        private RecordingProcessingRepository(
                AcquireMode mode,
                ClaimDerivation derivation,
                ObservationId observationId
        ) {
            this.mode = mode;
            this.derivation = derivation;
            this.observationId = observationId;
        }

        @Override
        public ClaimProcessingAcquireResult acquire(
                ClaimProcessingKey key
        ) {
            if (mode == AcquireMode.ACQUIRED) {
                return new ClaimProcessingAcquireResult.Acquired();
            }

            return new ClaimProcessingAcquireResult.AlreadyCompleted(
                    new ClaimProcessingReceipt(
                            new ClaimProcessingKey(
                                    observationId,
                                    derivation
                            ),
                            T0,
                            List.of(
                                    ClaimId.random()
                            )
                    )
            );
        }

        @Override
        public void complete(
                ClaimProcessingReceipt receipt
        ) {
            completed.add(receipt);
        }
    }

    private static final class RecordingClaimRepository
            implements ClaimRepository {

        private final List<Claim> claims =
                new ArrayList<>();

        @Override
        public void add(
                Claim claim
        ) {
            claims.add(claim);
        }

        @Override
        public Optional<Claim> findById(
                ClaimId id
        ) {
            return claims.stream()
                    .filter(
                            claim ->
                                    claim.id()
                                            .equals(id)
                    )
                    .findFirst();
        }
    }

    private static final class RecordingEventPublisher
            implements EventPublisher {

        private final List<EventEnvelope<?>> events =
                new ArrayList<>();

        @Override
        public void publish(
                EventEnvelope<? extends com.fold.kernel.events.IntegrationEvent>
                        event
        ) {
            events.add(event);
        }
    }

    private static final class SequenceClock
            implements FoldClock {

        private final List<Timestamp> values;
        private int index;

        private SequenceClock(
                List<Timestamp> values
        ) {
            this.values =
                    List.copyOf(values);
        }

        @Override
        public Timestamp now() {
            if (index >= values.size()) {
                return values.getLast();
            }

            return values.get(index++);
        }
    }

    private record Fixture(
            ProcessAcceptedObservationHandler handler,
            EventEnvelope<ObservationAcceptedV1> envelope,
            ClaimDerivation derivation,
            AtomicInteger derivationExecutions,
            MutableRule rule,
            RecordingProcessingRepository processingRepository,
            RecordingClaimRepository claimRepository,
            RecordingEventPublisher eventPublisher
    ) {
    }
}