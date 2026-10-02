package com.fold.modules.observations.application.feature.submitobservation;

import com.fold.kernel.events.EventEnvelope;
import com.fold.kernel.events.EventMetadata;
import com.fold.kernel.events.EventPublisher;
import com.fold.kernel.ids.ObservationId;
import com.fold.kernel.time.FoldClock;
import com.fold.kernel.time.Timestamp;
import com.fold.modules.observations.api.event.DuplicateObservationDetectedV1;
import com.fold.modules.observations.api.event.ObservationAcceptedV1;
import com.fold.modules.observations.api.event.ObservationRejectedV1;
import com.fold.modules.observations.domain.event.ObservationAccepted;
import com.fold.modules.observations.domain.event.ObservationRejected;
import com.fold.modules.observations.domain.model.Observation;
import com.fold.modules.observations.domain.policy.ObservationAcceptanceDecision;
import com.fold.modules.observations.domain.policy.ObservationAcceptancePolicy;
import com.fold.modules.observations.domain.policy.ObservationDeduplicationPolicy;
import com.fold.modules.observations.domain.policy.ObservationFingerprintPolicy;
import com.fold.modules.observations.domain.repository.ObservationAddResult;
import com.fold.modules.observations.domain.repository.ObservationRepository;
import com.fold.modules.observations.domain.value.ArrivedAt;
import com.fold.modules.observations.domain.value.ObservationDeduplicationResult;
import com.fold.modules.observations.domain.value.ObservationFingerprint;

import java.util.Objects;

/**
 * Executes the complete submission workflow for one normalized observation.
 *
 * <p>The handler orchestrates domain behavior but contains no observation
 * business rules of its own.</p>
 *
 * <p>The workflow is:</p>
 *
 * <ol>
 *     <li>calculate the canonical semantic fingerprint;</li>
 *     <li>perform an advisory duplicate lookup;</li>
 *     <li>create a received observation when no duplicate is known;</li>
 *     <li>evaluate the observation acceptance policy;</li>
 *     <li>apply the resulting aggregate lifecycle transition;</li>
 *     <li>attempt authoritative insertion;</li>
 *     <li>resolve a concurrent fingerprint conflict as a duplicate;</li>
 *     <li>publish the corresponding versioned integration event.</li>
 * </ol>
 *
 * <p>In production, observation insertion and integration-event publication
 * must participate in one transactional reliability boundary, normally by
 * persisting the integration event to a transactional outbox. A broker send
 * performed independently after database commit is not sufficient.</p>
 */
public final class SubmitObservationHandler
        implements SubmitObservationUseCase {

    private static final String EVENT_PRODUCER =
            "observations";

    private final ObservationFingerprintPolicy fingerprintPolicy;
    private final ObservationDeduplicationPolicy deduplicationPolicy;
    private final ObservationAcceptancePolicy acceptancePolicy;
    private final ObservationRepository repository;
    private final FoldClock clock;
    private final EventPublisher eventPublisher;

    public SubmitObservationHandler(
            ObservationFingerprintPolicy fingerprintPolicy,
            ObservationDeduplicationPolicy deduplicationPolicy,
            ObservationAcceptancePolicy acceptancePolicy,
            ObservationRepository repository,
            FoldClock clock,
            EventPublisher eventPublisher
    ) {
        this.fingerprintPolicy = Objects.requireNonNull(
                fingerprintPolicy,
                "fingerprintPolicy must not be null"
        );
        this.deduplicationPolicy = Objects.requireNonNull(
                deduplicationPolicy,
                "deduplicationPolicy must not be null"
        );
        this.acceptancePolicy = Objects.requireNonNull(
                acceptancePolicy,
                "acceptancePolicy must not be null"
        );
        this.repository = Objects.requireNonNull(
                repository,
                "repository must not be null"
        );
        this.clock = Objects.requireNonNull(
                clock,
                "clock must not be null"
        );
        this.eventPublisher = Objects.requireNonNull(
                eventPublisher,
                "eventPublisher must not be null"
        );
    }

    /**
     * Submits one normalized observation candidate.
     *
     * @param command the submission
     * @return the final submission outcome
     */
    @Override
    public SubmitObservationResult handle(
            SubmitObservationCommand command
    ) {
        Objects.requireNonNull(
                command,
                "command must not be null"
        );

        ObservationFingerprint fingerprint =
                fingerprintPolicy.calculate(
                        new ObservationFingerprintPolicy.Input(
                                command.type(),
                                command.subject(),
                                command.payload(),
                                command.origin(),
                                command.observedAt()
                        )
                );

        ObservationDeduplicationResult preliminary =
                deduplicationPolicy.evaluate(
                        fingerprint
                );

        if (preliminary
                instanceof ObservationDeduplicationResult.Duplicate duplicate) {

            return duplicate(
                    duplicate.existingObservationId(),
                    fingerprint,
                    clock.now()
            );
        }

        Timestamp arrivedAt =
                clock.now();

        Observation observation =
                Observation.receive(
                        ObservationId.random(),
                        command.type(),
                        command.subject(),
                        command.payload(),
                        command.origin(),
                        fingerprint,
                        command.observedAt(),
                        ArrivedAt.of(arrivedAt)
                );

        ObservationAcceptanceDecision decision =
                Objects.requireNonNull(
                        acceptancePolicy.evaluate(
                                observation
                        ),
                        "acceptance policy must not return null"
                );

        Timestamp decidedAt =
                clock.now();

        if (decision
                instanceof ObservationAcceptanceDecision.Accepted) {

            ObservationAccepted domainEvent =
                    observation.accept(decidedAt)
                            .fold(
                                    event -> event,
                                    error -> {
                                        throw new IllegalStateException(
                                                error.message()
                                        );
                                    }
                            );

            ObservationAddResult addResult =
                    repository.add(observation);

            if (addResult
                    instanceof ObservationAddResult.Duplicate duplicate) {

                return duplicate(
                        duplicate.existingObservationId(),
                        fingerprint,
                        clock.now()
                );
            }

            ObservationAcceptedV1 integrationEvent =
                    new ObservationAcceptedV1(
                            observation.id(),
                            domainEvent.occurredAt()
                    );

            publish(integrationEvent);

            return new SubmitObservationResult.Accepted(
                    observation.id(),
                    fingerprint
            );
        }

        ObservationAcceptanceDecision.Rejected rejected =
                (ObservationAcceptanceDecision.Rejected) decision;

        ObservationRejected domainEvent =
                observation.reject(
                                rejected.rejection(),
                                decidedAt
                        )
                        .fold(
                                event -> event,
                                error -> {
                                    throw new IllegalStateException(
                                            error.message()
                                    );
                                }
                        );

        ObservationAddResult addResult =
                repository.add(observation);

        if (addResult
                instanceof ObservationAddResult.Duplicate duplicate) {

            return duplicate(
                    duplicate.existingObservationId(),
                    fingerprint,
                    clock.now()
            );
        }

        ObservationRejectedV1 integrationEvent =
                new ObservationRejectedV1(
                        observation.id(),
                        domainEvent.rejection().code(),
                        domainEvent.rejection().message(),
                        domainEvent.occurredAt()
                );

        publish(integrationEvent);

        return new SubmitObservationResult.Rejected(
                observation.id(),
                fingerprint,
                domainEvent.rejection()
        );
    }

    private SubmitObservationResult duplicate(
            ObservationId existingObservationId,
            ObservationFingerprint fingerprint,
            Timestamp detectedAt
    ) {
        DuplicateObservationDetectedV1 event =
                new DuplicateObservationDetectedV1(
                        existingObservationId,
                        detectedAt
                );

        publish(event);

        return new SubmitObservationResult.Duplicate(
                existingObservationId,
                fingerprint
        );
    }

    private void publish(
            com.fold.kernel.events.IntegrationEvent event
    ) {
        Timestamp emittedAt =
                clock.now();

        EventEnvelope<com.fold.kernel.events.IntegrationEvent>
                envelope =
                new EventEnvelope<>(
                        EventMetadata.root(
                                emittedAt,
                                EVENT_PRODUCER
                        ),
                        event
                );

        eventPublisher.publish(envelope);
    }
}