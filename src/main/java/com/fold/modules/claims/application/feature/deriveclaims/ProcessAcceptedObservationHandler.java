package com.fold.modules.claims.application.feature.deriveclaims;

import com.fold.kernel.events.EventEnvelope;
import com.fold.kernel.events.EventMetadata;
import com.fold.kernel.events.EventPublisher;
import com.fold.kernel.ids.ClaimId;
import com.fold.kernel.time.FoldClock;
import com.fold.kernel.time.Timestamp;
import com.fold.modules.claims.api.event.ClaimCreatedV1;
import com.fold.modules.claims.application.idempotency.ClaimProcessingAcquireResult;
import com.fold.modules.claims.application.idempotency.ClaimProcessingKey;
import com.fold.modules.claims.application.idempotency.ClaimProcessingReceipt;
import com.fold.modules.claims.application.idempotency.ClaimProcessingRepository;
import com.fold.modules.claims.application.integration.observations.AcceptedObservationClaimInputProvider;
import com.fold.modules.claims.domain.derivation.ClaimDerivationInput;
import com.fold.modules.claims.domain.derivation.ClaimProposal;
import com.fold.modules.claims.domain.model.Claim;
import com.fold.modules.claims.domain.model.ClaimCreation;
import com.fold.modules.claims.domain.repository.ClaimRepository;
import com.fold.modules.claims.domain.value.ClaimDerivation;
import com.fold.modules.observations.api.event.ObservationAcceptedV1;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Performs authoritative Observation-to-Claim processing.
 *
 * <p>The handler remains Spring-free. The production transaction boundary is
 * applied by infrastructure.</p>
 *
 * <p>For each applicable derivation:</p>
 *
 * <ol>
 *     <li>atomically acquire its semantic processing key;</li>
 *     <li>skip derivation entirely when the key was already completed;</li>
 *     <li>derive zero or more Claim proposals;</li>
 *     <li>create and persist authoritative Claims;</li>
 *     <li>publish ClaimCreatedV1 through the transactional outbox;</li>
 *     <li>record completed processing, including zero-output processing.</li>
 * </ol>
 */
public final class ProcessAcceptedObservationHandler
        implements ProcessAcceptedObservationUseCase {

    private static final String EVENT_PRODUCER =
            "claims";

    private final AcceptedObservationClaimInputProvider inputProvider;
    private final ClaimDerivationEngine derivationEngine;
    private final ClaimProcessingRepository processingRepository;
    private final ClaimRepository claimRepository;
    private final FoldClock clock;
    private final EventPublisher eventPublisher;

    public ProcessAcceptedObservationHandler(
            AcceptedObservationClaimInputProvider inputProvider,
            ClaimDerivationEngine derivationEngine,
            ClaimProcessingRepository processingRepository,
            ClaimRepository claimRepository,
            FoldClock clock,
            EventPublisher eventPublisher
    ) {
        this.inputProvider = Objects.requireNonNull(
                inputProvider,
                "inputProvider must not be null"
        );

        this.derivationEngine = Objects.requireNonNull(
                derivationEngine,
                "derivationEngine must not be null"
        );

        this.processingRepository = Objects.requireNonNull(
                processingRepository,
                "processingRepository must not be null"
        );

        this.claimRepository = Objects.requireNonNull(
                claimRepository,
                "claimRepository must not be null"
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

    @Override
    public ProcessAcceptedObservationResult handle(
            EventEnvelope<ObservationAcceptedV1> envelope
    ) {
        Objects.requireNonNull(
                envelope,
                "envelope must not be null"
        );

        ClaimDerivationInput input =
                inputProvider.load(
                        envelope.payload()
                );

        List<ClaimDerivation> derivations =
                derivationEngine
                        .applicableDerivations(input);

        List<ProcessAcceptedObservationResult.BatchResult>
                results =
                new ArrayList<>();

        for (ClaimDerivation derivation : derivations) {

            ClaimProcessingKey processingKey =
                    new ClaimProcessingKey(
                            input.observationId(),
                            derivation
                    );

            ClaimProcessingAcquireResult acquireResult =
                    processingRepository.acquire(
                            processingKey
                    );

            if (acquireResult
                    instanceof ClaimProcessingAcquireResult
                    .AlreadyCompleted alreadyCompleted) {

                results.add(
                        new ProcessAcceptedObservationResult
                                .AlreadyCompleted(
                                alreadyCompleted.receipt()
                        )
                );

                continue;
            }

            ClaimDerivationBatch batch =
                    derivationEngine.derive(
                            input,
                            derivation
                    );

            List<ClaimId> producedClaimIds =
                    new ArrayList<>();

            for (ClaimProposal proposal : batch.proposals()) {

                Timestamp createdAt =
                        clock.now();

                ClaimCreation creation =
                        Claim.createNew(
                                proposal.proposition(),
                                proposal.origin(),
                                derivation,
                                proposal.confidence()
                                        .orElse(null),
                                proposal.observedAt()
                                        .orElse(null),
                                proposal.validTime()
                                        .orElse(null),
                                createdAt
                        );

                Claim claim =
                        creation.claim();

                claimRepository.add(claim);

                producedClaimIds.add(
                        claim.id()
                );

                publishCreated(
                        creation,
                        envelope
                );
            }

            ClaimProcessingReceipt receipt =
                    new ClaimProcessingReceipt(
                            processingKey,
                            clock.now(),
                            producedClaimIds
                    );

            processingRepository.complete(
                    receipt
            );

            results.add(
                    new ProcessAcceptedObservationResult
                            .Completed(receipt)
            );
        }

        return new ProcessAcceptedObservationResult(
                results
        );
    }

    private void publishCreated(
            ClaimCreation creation,
            EventEnvelope<ObservationAcceptedV1> cause
    ) {
        ClaimCreatedV1 integrationEvent =
                new ClaimCreatedV1(
                        creation.claim().id(),
                        creation.event().occurredAt()
                );

        Timestamp emittedAt =
                clock.now();

        EventMetadata metadata =
                EventMetadata.causedBy(
                        cause.metadata().correlationId(),
                        cause.metadata().eventId(),
                        emittedAt,
                        EVENT_PRODUCER
                );

        eventPublisher.publish(
                new EventEnvelope<>(
                        metadata,
                        integrationEvent
                )
        );
    }
}