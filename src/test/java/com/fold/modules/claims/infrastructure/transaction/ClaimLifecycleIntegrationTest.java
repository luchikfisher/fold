package com.fold.modules.claims.infrastructure.transaction;

import com.fold.kernel.events.EventMetadata;
import com.fold.kernel.ids.ClaimId;
import com.fold.modules.claims.api.event.ClaimInvalidatedV1;
import com.fold.modules.claims.api.event.ClaimLifecycleReasonV1;
import com.fold.modules.claims.api.event.ClaimReinstatedV1;
import com.fold.modules.claims.api.event.ClaimSupersededV1;
import com.fold.modules.claims.api.query.GetClaimQuery;
import com.fold.modules.claims.api.view.ClaimView;
import com.fold.modules.claims.application.feature.lifecycle.*;
import com.fold.modules.claims.domain.model.Claim;
import com.fold.modules.claims.domain.value.*;
import com.fold.testsupport.claims.ClaimLifecycleIntegrationTestSupport;
import com.fold.testsupport.claims.ClaimTestFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.aop.support.AopUtils;

import java.util.Map;

import static com.fold.modules.claims.application.feature.lifecycle.ClaimLifecycleResult.Rejection.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClaimLifecycleIntegrationTest extends ClaimLifecycleIntegrationTestSupport {

    @Test
    void wiredUseCaseShouldPersistRevisionsAndPublicEventsWithoutChangingCreationContext() {
        assertThat(AopUtils.isAopProxy(useCase)).isTrue();
        assertThat(useCase).isInstanceOf(TransactionalClaimLifecycleUseCase.class);
        Claim original = persistClaim();
        ClaimView before = queries.get(new GetClaimQuery(original.id())).orElseThrow();
        EventMetadata cause = EventMetadata.root(NOW, "decision-source");

        var invalidated = (ClaimLifecycleResult.Applied) useCase.handle(
                new InvalidateClaimCommand(original.id(), INITIAL, REASON, cause));
        var reinstated = (ClaimLifecycleResult.Applied) useCase.handle(
                new ReinstateClaimCommand(original.id(), new ClaimRevision(2), REASON, null));

        Claim restored = repository.findById(original.id()).orElseThrow();
        assertThat(restored.status()).isEqualTo(ClaimStatus.ACTIVE);
        assertThat(restored.revision()).isEqualTo(new ClaimRevision(3));
        assertThat(restored.revisionHistory()).containsExactly(
                original.revisionHistory().getFirst(), invalidated.revision(), reinstated.revision());
        assertThat(invalidated.revision().changedAt()).isEqualTo(NOW);
        assertThat(reinstated.revision().changedAt()).isEqualTo(NOW);

        ClaimView after = queries.get(new GetClaimQuery(original.id())).orElseThrow();
        assertThat(after).usingRecursiveComparison().ignoringFields("status", "revision", "lifecycle")
                .isEqualTo(before);
        assertThat(after.lifecycle()).extracting(ClaimView.LifecycleRevision::transition)
                .containsExactly("CREATED", "INVALIDATED", "REINSTATED");
        assertThat(eventCount()).isEqualTo(2);

        Map<String, Object> row = eventRow(ClaimInvalidatedV1.TYPE);
        assertThat(row.get("causation_id")).isEqualTo(cause.eventId().value());
        assertThat(row.get("correlation_id")).isEqualTo(cause.correlationId().value());
        assertThat(row.get("producer")).isEqualTo("claims");
        assertThat(row.get("event_version")).isEqualTo(1);
        assertThat(row.get("occurred_at")).isEqualTo(java.sql.Timestamp.from(NOW.value()));
        assertThat(row.get("emitted_at")).isEqualTo(java.sql.Timestamp.from(NOW.value()));
        assertThat(objectMapper.readValue((String) row.get("payload"), ClaimInvalidatedV1.class))
                .isEqualTo(new ClaimInvalidatedV1(original.id(), 2,
                        new ClaimLifecycleReasonV1(REASON.code(), REASON.message()), NOW));

        Map<String, Object> root = eventRow(ClaimReinstatedV1.TYPE);
        assertThat(root.get("causation_id")).isNull();
        assertThat(root.get("correlation_id")).isEqualTo(root.get("event_id"));
        assertThat(objectMapper.readValue((String) root.get("payload"), ClaimReinstatedV1.class))
                .isEqualTo(new ClaimReinstatedV1(original.id(), 3,
                        new ClaimLifecycleReasonV1(REASON.code(), REASON.message()), NOW));
    }

    @Test
    void supersessionShouldLinkDifferentPropositionsAndPreserveSuccessorAndOrigins() {
        Claim predecessor = persistClaim();
        Claim successor = Claim.createNew(
                new ClaimProposition(ClaimSubject.of("other", "different-subject"),
                        ClaimPredicate.of("different-predicate"), new ClaimObject.Text("replacement")),
                ClaimTestFixtures.origin(), ClaimTestFixtures.derivation(), null, null, null,
                ClaimTestFixtures.CREATED_AT).claim();
        tx().executeWithoutResult(status -> repository.add(successor));

        var result = (ClaimLifecycleResult.Applied) useCase.handle(
                new SupersedeClaimCommand(predecessor.id(), INITIAL, successor.id(), null, null));

        Claim restored = repository.findById(predecessor.id()).orElseThrow();
        assertThat(restored.status()).isEqualTo(ClaimStatus.SUPERSEDED);
        assertThat(restored.proposition()).isEqualTo(predecessor.proposition());
        assertThat(restored.origin()).isEqualTo(predecessor.origin());
        assertThat(restored.revisionHistory()).containsExactly(
                predecessor.revisionHistory().getFirst(), result.revision());
        assertThat(result.revision().relatedClaimId()).contains(successor.id());
        assertThat(result.revision().reason()).isEmpty();
        assertThat(repository.findById(successor.id()).orElseThrow())
                .usingRecursiveComparison().isEqualTo(successor);
        var view = queries.get(new GetClaimQuery(predecessor.id())).orElseThrow();
        assertThat(view.lifecycle().getLast().relatedClaimId()).isEqualTo(successor.id());
        assertThat(objectMapper.readValue((String) eventRow(ClaimSupersededV1.TYPE).get("payload"),
                ClaimSupersededV1.class))
                .isEqualTo(new ClaimSupersededV1(predecessor.id(), successor.id(), 2, null, NOW));
        assertThat(eventCount()).isEqualTo(1);
    }

    @Test
    void historicalRetriesShouldReturnOriginalDecisionEvenAfterFurtherTransitions() {
        Claim claim = persistClaim();
        var command = new InvalidateClaimCommand(claim.id(), INITIAL, REASON, null);
        var first = (ClaimLifecycleResult.Applied) useCase.handle(command);
        var reinstate = new ReinstateClaimCommand(claim.id(), new ClaimRevision(2), REASON, null);
        var second = (ClaimLifecycleResult.Applied) useCase.handle(reinstate);
        ClaimLifecycleReason laterReason = new ClaimLifecycleReason("LATER", "Later evidence");
        useCase.handle(new InvalidateClaimCommand(claim.id(), new ClaimRevision(3), laterReason, null));

        assertThat(useCase.handle(new InvalidateClaimCommand(claim.id(), INITIAL, REASON,
                EventMetadata.root(NOW, "redelivery"))))
                .isEqualTo(new ClaimLifecycleResult.AlreadyApplied(claim.id(), first.revision()));
        assertThat(useCase.handle(reinstate))
                .isEqualTo(new ClaimLifecycleResult.AlreadyApplied(claim.id(), second.revision()));
        assertThat(useCase.handle(new InvalidateClaimCommand(claim.id(), INITIAL, laterReason, null)))
                .isEqualTo(new ClaimLifecycleResult.RevisionConflict(INITIAL, new ClaimRevision(4)));
        assertThat(useCase.handle(new ReinstateClaimCommand(claim.id(), INITIAL, REASON, null)))
                .isInstanceOf(ClaimLifecycleResult.RevisionConflict.class);
        assertThat(repository.findById(claim.id()).orElseThrow().revision()).isEqualTo(new ClaimRevision(4));
        assertThat(eventCount()).isEqualTo(3);
    }

    @Test
    void supersessionReplayShouldNotRevalidateSuccessorsCurrentState() {
        Claim predecessor = persistClaim();
        Claim successor = persistClaim();
        var command = new SupersedeClaimCommand(predecessor.id(), INITIAL, successor.id(), REASON, null);
        var applied = (ClaimLifecycleResult.Applied) useCase.handle(command);
        useCase.handle(new InvalidateClaimCommand(successor.id(), INITIAL, REASON, null));

        assertThat(useCase.handle(command))
                .isEqualTo(new ClaimLifecycleResult.AlreadyApplied(predecessor.id(), applied.revision()));
        assertThat(useCase.handle(new SupersedeClaimCommand(predecessor.id(), INITIAL,
                ClaimId.random(), REASON, null))).isInstanceOf(ClaimLifecycleResult.RevisionConflict.class);
        assertThat(useCase.handle(new SupersedeClaimCommand(predecessor.id(), INITIAL,
                successor.id(), null, null))).isInstanceOf(ClaimLifecycleResult.RevisionConflict.class);
        assertThat(eventCount()).isEqualTo(2);
    }

    @Test
    void manyPredecessorsAndChainsShouldPreserveLineageButCannotCycle() {
        Claim a = persistClaim();
        Claim b = persistClaim();
        Claim c = persistClaim();
        Claim d = persistClaim();
        assertThat(useCase.handle(new SupersedeClaimCommand(a.id(), INITIAL, b.id(), REASON, null)))
                .isInstanceOf(ClaimLifecycleResult.Applied.class);
        assertThat(useCase.handle(new SupersedeClaimCommand(d.id(), INITIAL, b.id(), null, null)))
                .isInstanceOf(ClaimLifecycleResult.Applied.class);
        assertThat(useCase.handle(new SupersedeClaimCommand(b.id(), INITIAL, c.id(), null, null)))
                .isInstanceOf(ClaimLifecycleResult.Applied.class);
        assertThat(useCase.handle(new SupersedeClaimCommand(c.id(), INITIAL, a.id(), null, null)))
                .isEqualTo(new ClaimLifecycleResult.Rejected(SUCCESSOR_NOT_ACTIVE));
        assertThat(repository.findById(a.id()).orElseThrow().revisionHistory().getLast().relatedClaimId())
                .contains(b.id());
        assertThat(eventCount()).isEqualTo(3);
    }

    @Test
    void invalidCommandsShouldNotAppendHistoryOrPublish() {
        Claim claim = persistClaim();
        Claim successor = persistClaim();
        assertThat(useCase.handle(new InvalidateClaimCommand(ClaimId.random(), INITIAL, REASON, null)))
                .isInstanceOf(ClaimLifecycleResult.NotFound.class);
        assertThat(useCase.handle(new InvalidateClaimCommand(claim.id(), new ClaimRevision(9), REASON, null)))
                .isInstanceOf(ClaimLifecycleResult.RevisionConflict.class);
        assertThat(useCase.handle(new ReinstateClaimCommand(claim.id(), INITIAL, REASON, null)))
                .isEqualTo(new ClaimLifecycleResult.Rejected(INVALID_TRANSITION));
        assertThat(useCase.handle(new SupersedeClaimCommand(claim.id(), INITIAL, ClaimId.random(), null, null)))
                .isEqualTo(new ClaimLifecycleResult.Rejected(SUCCESSOR_NOT_FOUND));
        useCase.handle(new InvalidateClaimCommand(successor.id(), INITIAL, REASON, null));
        assertThat(useCase.handle(new SupersedeClaimCommand(claim.id(), INITIAL, successor.id(), null, null)))
                .isEqualTo(new ClaimLifecycleResult.Rejected(SUCCESSOR_NOT_ACTIVE));
        assertThat(useCase.handle(new InvalidateClaimCommand(successor.id(), new ClaimRevision(2), REASON, null)))
                .isEqualTo(new ClaimLifecycleResult.Rejected(INVALID_TRANSITION));
        assertThat(useCase.handle(new SupersedeClaimCommand(successor.id(), new ClaimRevision(2), claim.id(), null, null)))
                .isEqualTo(new ClaimLifecycleResult.Rejected(INVALID_TRANSITION));
        assertThat(repository.findById(claim.id()).orElseThrow().revision()).isEqualTo(INITIAL);
        assertThat(eventCount()).isEqualTo(1);
    }

    @Test
    void supersededClaimShouldRemainTerminalThroughEveryEntryPoint() {
        Claim claim = persistClaim();
        Claim successor = persistClaim();
        useCase.handle(new SupersedeClaimCommand(claim.id(), INITIAL, successor.id(), null, null));
        ClaimRevision revision = new ClaimRevision(2);
        assertThat(useCase.handle(new ReinstateClaimCommand(claim.id(), revision, REASON, null)))
                .isEqualTo(new ClaimLifecycleResult.Rejected(INVALID_TRANSITION));
        assertThat(useCase.handle(new InvalidateClaimCommand(claim.id(), revision, REASON, null)))
                .isEqualTo(new ClaimLifecycleResult.Rejected(INVALID_TRANSITION));
        assertThat(useCase.handle(new SupersedeClaimCommand(claim.id(), revision, successor.id(), null, null)))
                .isEqualTo(new ClaimLifecycleResult.Rejected(INVALID_TRANSITION));
        assertThat(eventCount()).isEqualTo(1);
    }

    @ParameterizedTest
    @EnumSource(value = ClaimLifecycleTransition.class, names = {"INVALIDATED", "REINSTATED", "SUPERSEDED"})
    void failureAfterOutboxInsertShouldRollBackStatusHistoryAndEventAndAllowRetry(ClaimLifecycleTransition transition) {
        Claim claim = persistClaim();
        Claim successor = persistClaim();
        if (transition == ClaimLifecycleTransition.REINSTATED) {
            useCase.handle(new InvalidateClaimCommand(claim.id(), INITIAL, REASON, null));
        }
        Claim before = repository.findById(claim.id()).orElseThrow();
        long previousEvents = eventCount();
        java.util.function.Supplier<ClaimLifecycleResult> operation = switch (transition) {
            case INVALIDATED -> () -> useCase.handle(new InvalidateClaimCommand(claim.id(), before.revision(), REASON, null));
            case REINSTATED -> () -> useCase.handle(new ReinstateClaimCommand(claim.id(), before.revision(), REASON, null));
            case SUPERSEDED -> () -> useCase.handle(new SupersedeClaimCommand(claim.id(), before.revision(), successor.id(), REASON, null));
            case CREATED -> throw new AssertionError("Not a lifecycle operation");
        };
        publisher.failAfterWrite.set(true);
        assertThatThrownBy(operation::get)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Failure after durable outbox insert");
        assertThat(repository.findById(claim.id()).orElseThrow()).usingRecursiveComparison().isEqualTo(before);
        assertThat(eventCount()).isEqualTo(previousEvents);
        assertThat(operation.get()).isInstanceOf(ClaimLifecycleResult.Applied.class);
        assertThat(eventCount()).isEqualTo(previousEvents + 1);
    }

    private Map<String, Object> eventRow(String type) {
        return jdbc.queryForMap("""
                SELECT event_id, correlation_id, causation_id, producer, event_version,
                       occurred_at, emitted_at, payload::text AS payload
                FROM integration_outbox WHERE event_type = ?
                """, type);
    }
}
