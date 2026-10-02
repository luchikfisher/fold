package com.fold.modules.observations.application.feature.submitobservation;

import com.fold.kernel.events.EventEnvelope;
import com.fold.kernel.ids.ObservationId;
import com.fold.kernel.ids.SourceId;
import com.fold.kernel.time.FoldClock;
import com.fold.kernel.time.Timestamp;
import com.fold.modules.observations.api.event.DuplicateObservationDetectedV1;
import com.fold.modules.observations.api.event.ObservationAcceptedV1;
import com.fold.modules.observations.api.event.ObservationRejectedV1;
import com.fold.modules.observations.domain.model.*;
import com.fold.modules.observations.domain.policy.*;
import com.fold.modules.observations.domain.repository.InMemoryObservationRepository;
import com.fold.modules.observations.domain.repository.ObservationAddResult;
import com.fold.modules.observations.domain.repository.ObservationRepository;
import com.fold.modules.observations.domain.value.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SubmitObservationHandlerTest {

    private static final Timestamp NOW =
            Timestamp.parse("2026-09-28T17:00:00Z");

    private InMemoryObservationRepository repository;
    private RecordingEventPublisher publisher;

    @BeforeEach
    void setUp() {
        repository =
                new InMemoryObservationRepository();

        publisher =
                new RecordingEventPublisher();
    }

    @Test
    void validUniqueSubmissionShouldCreateAcceptedObservation() {
        SubmitObservationHandler handler =
                handler(
                        new DefaultObservationAcceptancePolicy()
                );

        SubmitObservationResult result =
                handler.handle(command());

        assertThat(result)
                .isInstanceOf(
                        SubmitObservationResult.Accepted.class
                );

        SubmitObservationResult.Accepted accepted =
                (SubmitObservationResult.Accepted) result;

        assertThat(
                repository.findById(
                        accepted.observationId()
                )
        ).isPresent();

        assertThat(
                repository.findById(
                                accepted.observationId()
                        )
                        .orElseThrow()
                        .isAccepted()
        ).isTrue();

        assertThat(repository.size())
                .isEqualTo(1);
    }

    @Test
    void acceptedSubmissionShouldPublishAcceptedIntegrationEvent() {
        SubmitObservationHandler handler =
                handler(
                        new DefaultObservationAcceptancePolicy()
                );

        SubmitObservationResult.Accepted result =
                (SubmitObservationResult.Accepted)
                        handler.handle(command());

        assertThat(publisher.size())
                .isEqualTo(1);

        EventEnvelope<?> envelope =
                publisher.events().getFirst();

        assertThat(envelope.payload())
                .isInstanceOf(
                        ObservationAcceptedV1.class
                );

        ObservationAcceptedV1 event =
                (ObservationAcceptedV1)
                        envelope.payload();

        assertThat(event.observationId())
                .isEqualTo(result.observationId());

        assertThat(envelope.metadata().producer())
                .isEqualTo("observations");

        assertThat(envelope.metadata().isRoot())
                .isTrue();
    }

    @Test
    void rejectedSubmissionShouldBePersistedAsRejected() {
        ObservationRejection rejection =
                ObservationRejection.of(
                        "unsupported-semantics",
                        "The observation is not semantically admissible"
                );

        ObservationAcceptancePolicy rejectingPolicy =
                observation ->
                        ObservationAcceptanceDecision.rejected(
                                rejection
                        );

        SubmitObservationHandler handler =
                handler(rejectingPolicy);

        SubmitObservationResult result =
                handler.handle(command());

        assertThat(result)
                .isInstanceOf(
                        SubmitObservationResult.Rejected.class
                );

        SubmitObservationResult.Rejected rejected =
                (SubmitObservationResult.Rejected) result;

        assertThat(
                repository.findById(
                                rejected.observationId()
                        )
                        .orElseThrow()
                        .isRejected()
        ).isTrue();

        assertThat(
                repository.findById(
                                rejected.observationId()
                        )
                        .orElseThrow()
                        .rejection()
        ).contains(rejection);
    }

    @Test
    void rejectedSubmissionShouldPublishRejectedIntegrationEvent() {
        ObservationRejection rejection =
                ObservationRejection.of(
                        "unsupported-semantics",
                        "The observation is not semantically admissible"
                );

        SubmitObservationHandler handler =
                handler(
                        observation ->
                                ObservationAcceptanceDecision.rejected(
                                        rejection
                                )
                );

        handler.handle(command());

        assertThat(publisher.size())
                .isEqualTo(1);

        assertThat(
                publisher.events()
                        .getFirst()
                        .payload()
        )
                .isInstanceOf(
                        ObservationRejectedV1.class
                );

        ObservationRejectedV1 event =
                (ObservationRejectedV1)
                        publisher.events()
                                .getFirst()
                                .payload();

        assertThat(event.rejectionCode())
                .isEqualTo(
                        "unsupported-semantics"
                );
    }

    @Test
    void secondEquivalentSubmissionShouldResolveToExistingObservation() {
        SubmitObservationHandler handler =
                handler(
                        new DefaultObservationAcceptancePolicy()
                );

        SubmitObservationResult.Accepted first =
                (SubmitObservationResult.Accepted)
                        handler.handle(command());

        SubmitObservationResult second =
                handler.handle(command());

        assertThat(second)
                .isInstanceOf(
                        SubmitObservationResult.Duplicate.class
                );

        SubmitObservationResult.Duplicate duplicate =
                (SubmitObservationResult.Duplicate) second;

        assertThat(
                duplicate.existingObservationId()
        ).isEqualTo(
                first.observationId()
        );

        assertThat(repository.size())
                .isEqualTo(1);
    }

    @Test
    void duplicateSubmissionShouldPublishDuplicateEvent() {
        SubmitObservationHandler handler =
                handler(
                        new DefaultObservationAcceptancePolicy()
                );

        SubmitObservationResult.Accepted first =
                (SubmitObservationResult.Accepted)
                        handler.handle(command());

        handler.handle(command());

        assertThat(publisher.size())
                .isEqualTo(2);

        assertThat(
                publisher.events()
                        .get(1)
                        .payload()
        )
                .isInstanceOf(
                        DuplicateObservationDetectedV1.class
                );

        DuplicateObservationDetectedV1 event =
                (DuplicateObservationDetectedV1)
                        publisher.events()
                                .get(1)
                                .payload();

        assertThat(
                event.existingObservationId()
        ).isEqualTo(
                first.observationId()
        );
    }

    @Test
    void differentSemanticPayloadShouldCreateDifferentObservation() {
        SubmitObservationHandler handler =
                handler(
                        new DefaultObservationAcceptancePolicy()
                );

        SubmitObservationResult first =
                handler.handle(command());

        SubmitObservationCommand changed =
                new SubmitObservationCommand(
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
                                                "Different Ltd"
                                        )
                                )
                        ),
                        origin(),
                        observedAt()
                );

        SubmitObservationResult second =
                handler.handle(changed);

        assertThat(first)
                .isInstanceOf(
                        SubmitObservationResult.Accepted.class
                );

        assertThat(second)
                .isInstanceOf(
                        SubmitObservationResult.Accepted.class
                );

        assertThat(repository.size())
                .isEqualTo(2);
    }

    @Test
    void arrivalTimeMustComeFromFoldClock() {
        SubmitObservationHandler handler =
                handler(
                        new DefaultObservationAcceptancePolicy()
                );

        SubmitObservationResult.Accepted result =
                (SubmitObservationResult.Accepted)
                        handler.handle(command());

        assertThat(
                repository.findById(
                                result.observationId()
                        )
                        .orElseThrow()
                        .arrivedAt()
                        .value()
        ).isEqualTo(NOW);
    }

    @Test
    void fingerprintShouldBeCalculatedByConfiguredPolicy() {
        Sha256ObservationFingerprintPolicy fingerprintPolicy =
                new Sha256ObservationFingerprintPolicy();

        SubmitObservationHandler handler =
                new SubmitObservationHandler(
                        fingerprintPolicy,
                        new ObservationDeduplicationPolicy(
                                repository
                        ),
                        new DefaultObservationAcceptancePolicy(),
                        repository,
                        fixedClock(),
                        publisher
                );

        SubmitObservationResult.Accepted result =
                (SubmitObservationResult.Accepted)
                        handler.handle(command());

        ObservationFingerprintPolicy.Input input =
                new ObservationFingerprintPolicy.Input(
                        command().type(),
                        command().subject(),
                        command().payload(),
                        command().origin(),
                        command().observedAt()
                );

        assertThat(result.fingerprint())
                .isEqualTo(
                        fingerprintPolicy.calculate(input)
                );
    }

    @Test
    void concurrentInsertionDetectedAtAddShouldResolveAsDuplicate() {
        ObservationId concurrentObservationId =
                ObservationId.random();

        ObservationRepository racingRepository =
                new ObservationRepository() {

                    @Override
                    public ObservationAddResult add(
                            Observation observation
                    ) {
                        return ObservationAddResult.duplicate(
                                concurrentObservationId
                        );
                    }

                    @Override
                    public java.util.Optional<Observation> findById(
                            ObservationId id
                    ) {
                        return java.util.Optional.empty();
                    }

                    @Override
                    public java.util.Optional<Observation> findByFingerprint(
                            ObservationFingerprint fingerprint
                    ) {
                        return java.util.Optional.empty();
                    }
                };

        RecordingEventPublisher racingPublisher =
                new RecordingEventPublisher();

        SubmitObservationHandler handler =
                new SubmitObservationHandler(
                        new Sha256ObservationFingerprintPolicy(),
                        new ObservationDeduplicationPolicy(
                                racingRepository
                        ),
                        new DefaultObservationAcceptancePolicy(),
                        racingRepository,
                        fixedClock(),
                        racingPublisher
                );

        SubmitObservationResult result =
                handler.handle(command());

        assertThat(result)
                .isInstanceOf(
                        SubmitObservationResult.Duplicate.class
                );

        SubmitObservationResult.Duplicate duplicate =
                (SubmitObservationResult.Duplicate) result;

        assertThat(
                duplicate.existingObservationId()
        ).isEqualTo(
                concurrentObservationId
        );

        assertThat(racingPublisher.size())
                .isEqualTo(1);

        assertThat(
                racingPublisher.events()
                        .getFirst()
                        .payload()
        )
                .isInstanceOf(
                        DuplicateObservationDetectedV1.class
                );
    }

    private SubmitObservationHandler handler(
            ObservationAcceptancePolicy acceptancePolicy
    ) {
        return new SubmitObservationHandler(
                new Sha256ObservationFingerprintPolicy(),
                new ObservationDeduplicationPolicy(
                        repository
                ),
                acceptancePolicy,
                repository,
                fixedClock(),
                publisher
        );
    }

    private static FoldClock fixedClock() {
        return () -> NOW;
    }

    private static SubmitObservationCommand command() {
        return new SubmitObservationCommand(
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
                origin(),
                observedAt()
        );
    }

    private static ObservationOrigin origin() {
        return ObservationOrigin.fromRecord(
                SOURCE_ID,
                "record-123"
        );
    }

    private static final SourceId SOURCE_ID =
            new SourceId(
                    java.util.UUID.fromString(
                            "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"
                    )
            );

    private static ObservedAt observedAt() {
        return ObservedAt.of(
                Timestamp.parse(
                        "2026-09-28T10:00:00Z"
                )
        );
    }
}