package com.fold.modules.claims.application.feature.lifecycle;

import com.fold.kernel.events.EventEnvelope;
import com.fold.kernel.events.EventMetadata;
import com.fold.kernel.events.EventPublisher;
import com.fold.kernel.events.IntegrationEvent;
import com.fold.kernel.ids.ClaimId;
import com.fold.kernel.time.FoldClock;
import com.fold.kernel.time.Timestamp;
import com.fold.modules.claims.api.event.ClaimInvalidatedV1;
import com.fold.modules.claims.api.event.ClaimLifecycleReasonV1;
import com.fold.modules.claims.api.event.ClaimReinstatedV1;
import com.fold.modules.claims.api.event.ClaimSupersededV1;
import com.fold.modules.claims.domain.model.Claim;
import com.fold.modules.claims.domain.repository.ClaimRepository;
import com.fold.modules.claims.domain.value.ClaimLifecycleReason;
import com.fold.modules.claims.domain.value.ClaimLifecycleRevision;
import com.fold.modules.claims.domain.value.ClaimLifecycleTransition;
import com.fold.modules.claims.domain.value.ClaimRevision;
import com.fold.modules.claims.domain.value.ClaimStatus;

import java.util.List;
import java.util.Objects;

import static com.fold.modules.claims.application.feature.lifecycle.ClaimLifecycleResult.Rejection.*;

/**
 * Coordinates explicit lifecycle decisions. Infrastructure supplies the transaction;
 * the aggregate owns transitions and the repository owns locking and durable writes.
 */
public final class ClaimLifecycleHandler implements ClaimLifecycleUseCase {

    private final ClaimRepository repository;
    private final FoldClock clock;
    private final EventPublisher publisher;

    public ClaimLifecycleHandler(
            ClaimRepository repository,
            FoldClock clock,
            EventPublisher publisher
    ) {
        this.repository = Objects.requireNonNull(repository);
        this.clock = Objects.requireNonNull(clock);
        this.publisher = Objects.requireNonNull(publisher);
    }

    @Override
    public ClaimLifecycleResult handle(InvalidateClaimCommand command) {
        Objects.requireNonNull(command);
        return handle(command.claimId(), command.expectedRevision(),
                ClaimLifecycleTransition.INVALIDATED, command.reason(), null, command.cause());
    }

    @Override
    public ClaimLifecycleResult handle(ReinstateClaimCommand command) {
        Objects.requireNonNull(command);
        return handle(command.claimId(), command.expectedRevision(),
                ClaimLifecycleTransition.REINSTATED, command.reason(), null, command.cause());
    }

    @Override
    public ClaimLifecycleResult handle(SupersedeClaimCommand command) {
        Objects.requireNonNull(command);
        return handle(command.claimId(), command.expectedRevision(),
                ClaimLifecycleTransition.SUPERSEDED, command.reason(), command.successorClaimId(), command.cause());
    }

    private ClaimLifecycleResult handle(
            ClaimId id,
            ClaimRevision expected,
            ClaimLifecycleTransition transition,
            ClaimLifecycleReason reason,
            ClaimId successorId,
            EventMetadata cause
    ) {
        List<ClaimId> locked = repository.lockByIds(
                successorId == null ? List.of(id) : List.of(id, successorId));
        if (!locked.contains(id)) {
            return new ClaimLifecycleResult.NotFound(id);
        }
        Claim claim = repository.findById(id).orElseThrow();

        // Compare the original next revision, not the latest status. A replay
        // must remain a replay after reinstatement or later successor changes.
        if (claim.revision().compareTo(expected) > 0) {
            ClaimLifecycleRevision recorded = claim.revisionHistory().stream()
                    .filter(revision -> revision.revision().equals(expected.next()))
                    .findFirst().orElseThrow();
            if (recorded.transition() == transition
                    && Objects.equals(recorded.reason().orElse(null), reason)
                    && Objects.equals(recorded.relatedClaimId().orElse(null), successorId)) {
                return new ClaimLifecycleResult.AlreadyApplied(id, recorded);
            }
        }
        if (!claim.revision().equals(expected)) {
            return new ClaimLifecycleResult.RevisionConflict(expected, claim.revision());
        }

        ClaimStatus required = transition == ClaimLifecycleTransition.REINSTATED
                ? ClaimStatus.INVALIDATED : ClaimStatus.ACTIVE;
        if (claim.status() != required) {
            return new ClaimLifecycleResult.Rejected(INVALID_TRANSITION);
        }

        Claim successor = null;
        if (successorId != null) {
            if (!locked.contains(successorId)) {
                return new ClaimLifecycleResult.Rejected(SUCCESSOR_NOT_FOUND);
            }
            successor = repository.findById(successorId).orElseThrow();
            if (!successor.isActive()) {
                return new ClaimLifecycleResult.Rejected(SUCCESSOR_NOT_ACTIVE);
            }
        }

        Timestamp changedAt = clock.now();
        if (changedAt.isBefore(claim.revisionHistory().getLast().changedAt())
                || (successor != null && changedAt.isBefore(
                        successor.revisionHistory().getLast().changedAt()))) {
            return new ClaimLifecycleResult.Rejected(TIME_REGRESSION);
        }

        ClaimLifecycleReasonV1 publicReason = reason == null ? null
                : new ClaimLifecycleReasonV1(reason.code(), reason.message());
        IntegrationEvent event = switch (transition) {
            case INVALIDATED -> {
                var domainEvent = claim.invalidate(reason, changedAt);
                yield new ClaimInvalidatedV1(
                        id, domainEvent.revision().value(), publicReason, domainEvent.occurredAt());
            }
            case REINSTATED -> {
                var domainEvent = claim.reinstate(reason, changedAt);
                yield new ClaimReinstatedV1(
                        id, domainEvent.revision().value(), publicReason, domainEvent.occurredAt());
            }
            case SUPERSEDED -> {
                var domainEvent = claim.supersedeBy(successorId, reason, changedAt);
                yield new ClaimSupersededV1(id, domainEvent.successorClaimId(),
                        domainEvent.revision().value(), publicReason, domainEvent.occurredAt());
            }
            case CREATED -> throw new IllegalArgumentException("Creation is not a lifecycle command");
        };

        repository.appendLifecycleRevision(claim, expected);
        EventMetadata metadata = cause == null
                ? EventMetadata.root(changedAt, "claims")
                : EventMetadata.causedBy(cause.correlationId(), cause.eventId(), changedAt, "claims");
        publisher.publish(new EventEnvelope<>(metadata, event));
        return new ClaimLifecycleResult.Applied(id, claim.revisionHistory().getLast());
    }
}
