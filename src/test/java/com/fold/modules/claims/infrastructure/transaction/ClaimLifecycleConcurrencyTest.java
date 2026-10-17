package com.fold.modules.claims.infrastructure.transaction;

import com.fold.kernel.ids.ClaimId;
import com.fold.modules.claims.application.feature.lifecycle.*;
import com.fold.modules.claims.domain.model.Claim;
import com.fold.modules.claims.domain.value.ClaimLifecycleReason;
import com.fold.modules.claims.domain.value.ClaimRevision;
import com.fold.modules.claims.domain.value.ClaimStatus;
import com.fold.testsupport.claims.ClaimLifecycleIntegrationTestSupport;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static com.fold.modules.claims.application.feature.lifecycle.ClaimLifecycleResult.Rejection.*;
import static org.assertj.core.api.Assertions.assertThat;

class ClaimLifecycleConcurrencyTest extends ClaimLifecycleIntegrationTestSupport {

    @Test
    void simultaneousIdenticalDecisionsShouldHaveOneRevisionAndOneEvent() throws Exception {
        Claim claim = persistClaim();
        var command = new InvalidateClaimCommand(claim.id(), INITIAL, REASON, null);
        var results = contend(() -> useCase.handle(command), () -> useCase.handle(command), () -> {});

        var first = (ClaimLifecycleResult.Applied) results.getFirst();
        assertThat(results.getLast()).isEqualTo(new ClaimLifecycleResult.AlreadyApplied(claim.id(), first.revision()));
        assertThat(repository.findById(claim.id()).orElseThrow().revisionHistory()).hasSize(2);
        assertThat(eventCount()).isEqualTo(1);
    }

    @Test
    void differentDecisionsAgainstSameRevisionShouldConflictWithoutLostUpdates() throws Exception {
        Claim claim = persistClaim();
        Claim successor = persistClaim();
        var results = contend(
                () -> useCase.handle(new InvalidateClaimCommand(claim.id(), INITIAL, REASON, null)),
                () -> useCase.handle(new SupersedeClaimCommand(claim.id(), INITIAL, successor.id(), null, null)),
                () -> {});

        assertThat(results.getFirst()).isInstanceOf(ClaimLifecycleResult.Applied.class);
        assertThat(results.getLast()).isEqualTo(new ClaimLifecycleResult.RevisionConflict(INITIAL, new ClaimRevision(2)));
        assertThat(repository.findById(claim.id()).orElseThrow().status()).isEqualTo(ClaimStatus.INVALIDATED);
        assertThat(eventCount()).isEqualTo(1);
    }

    @Test
    void reciprocalSupersessionShouldCommitOnlyOneDirection() throws Exception {
        Claim a = persistClaim();
        Claim b = persistClaim();
        var results = contend(
                () -> useCase.handle(new SupersedeClaimCommand(a.id(), INITIAL, b.id(), null, null)),
                () -> useCase.handle(new SupersedeClaimCommand(b.id(), INITIAL, a.id(), null, null)),
                () -> {});

        assertThat(results.getFirst()).isInstanceOf(ClaimLifecycleResult.Applied.class);
        assertThat(results.getLast()).isEqualTo(new ClaimLifecycleResult.Rejected(SUCCESSOR_NOT_ACTIVE));
        assertThat(repository.findById(a.id()).orElseThrow().status()).isEqualTo(ClaimStatus.SUPERSEDED);
        assertThat(repository.findById(b.id()).orElseThrow().status()).isEqualTo(ClaimStatus.ACTIVE);
        assertThat(eventCount()).isEqualTo(1);
    }

    @Test
    void supersessionShouldAcquireLowerIdBeforeHigherIdEvenWhenTargetIsHigher() throws Exception {
        Claim a = persistClaim();
        Claim b = persistClaim();
        ClaimId low = a.id().asString().compareTo(b.id().asString()) < 0 ? a.id() : b.id();
        ClaimId high = low.equals(a.id()) ? b.id() : a.id();

        var results = contend(
                () -> useCase.handle(new InvalidateClaimCommand(low, INITIAL, REASON, null)),
                () -> useCase.handle(new SupersedeClaimCommand(high, INITIAL, low, null, null)),
                () -> tx().executeWithoutResult(status -> {
                    // The supersession is provably blocked on low. If it had
                    // locked high first, this independent NOWAIT would fail.
                    assertThat(jdbc.queryForObject(
                            "SELECT id FROM claims WHERE id = ? FOR UPDATE NOWAIT",
                            java.util.UUID.class, high.value())).isEqualTo(high.value());
                }));

        assertThat(results.getLast()).isEqualTo(new ClaimLifecycleResult.Rejected(SUCCESSOR_NOT_ACTIVE));
        assertThat(repository.findById(high).orElseThrow().revision()).isEqualTo(INITIAL);
        assertThat(eventCount()).isEqualTo(1);
    }

    @Test
    void successorInvalidationShouldWaitForSupersessionThenLeaveHistoricalLinkIntact() throws Exception {
        Claim predecessor = persistClaim();
        Claim successor = persistClaim();
        var results = contend(
                () -> useCase.handle(new SupersedeClaimCommand(predecessor.id(), INITIAL, successor.id(), REASON, null)),
                () -> useCase.handle(new InvalidateClaimCommand(successor.id(), INITIAL, REASON, null)),
                () -> {});

        assertThat(results).allMatch(ClaimLifecycleResult.Applied.class::isInstance);
        assertThat(repository.findById(predecessor.id()).orElseThrow().revisionHistory().getLast().relatedClaimId())
                .contains(successor.id());
        assertThat(repository.findById(successor.id()).orElseThrow().status()).isEqualTo(ClaimStatus.INVALIDATED);
        assertThat(eventCount()).isEqualTo(2);
    }

    @Test
    void concurrentRetriesWithDifferentReasonsMustNotBeMistakenForReplay() throws Exception {
        Claim claim = persistClaim();
        var results = contend(
                () -> useCase.handle(new InvalidateClaimCommand(claim.id(), INITIAL, REASON, null)),
                () -> useCase.handle(new InvalidateClaimCommand(claim.id(), INITIAL,
                        new ClaimLifecycleReason(REASON.code(), "Different explanation"), null)),
                () -> {});
        assertThat(results.getLast()).isInstanceOf(ClaimLifecycleResult.RevisionConflict.class);
        assertThat(eventCount()).isEqualTo(1);
    }

    @Test
    void rollbackShouldReleaseLocksAndLetWaitingRetryApplyOnce() throws Exception {
        Claim claim = persistClaim();
        var command = new InvalidateClaimCommand(claim.id(), INITIAL, REASON, null);
        var results = contend(() -> useCase.handle(command), () -> useCase.handle(command), () -> {}, true);

        assertThat(results.getLast()).isInstanceOf(ClaimLifecycleResult.Applied.class);
        assertThat(repository.findById(claim.id()).orElseThrow().revisionHistory()).hasSize(2);
        assertThat(eventCount()).isEqualTo(1);
    }

    /** Hold the first real transaction open and prove the second waits in PostgreSQL. */
    private List<ClaimLifecycleResult> contend(
            Supplier<ClaimLifecycleResult> first,
            Supplier<ClaimLifecycleResult> second,
            Runnable whileBlocked
    ) throws Exception {
        return contend(first, second, whileBlocked, false);
    }

    private List<ClaimLifecycleResult> contend(
            Supplier<ClaimLifecycleResult> first,
            Supplier<ClaimLifecycleResult> second,
            Runnable whileBlocked,
            boolean rollbackFirst
    ) throws Exception {
        CountDownLatch firstApplied = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        CountDownLatch secondConnected = new CountDownLatch(1);
        AtomicInteger secondPid = new AtomicInteger();
        try (var executor = Executors.newFixedThreadPool(2)) {
            var firstResult = executor.submit(() -> tx().execute(status -> {
                var result = first.get();
                firstApplied.countDown();
                await(release);
                if (rollbackFirst) {
                    status.setRollbackOnly();
                }
                return result;
            }));
            try {
                await(firstApplied);
                var secondResult = executor.submit(() -> tx().execute(status -> {
                    secondPid.set(jdbc.queryForObject("SELECT pg_backend_pid()", Integer.class));
                    secondConnected.countDown();
                    return second.get();
                }));
                await(secondConnected);
                awaitDatabaseLockWait(secondPid.get());
                whileBlocked.run();
                release.countDown();
                return List.of(firstResult.get(10, TimeUnit.SECONDS), secondResult.get(10, TimeUnit.SECONDS));
            } finally {
                release.countDown();
            }
        }
    }

    private void awaitDatabaseLockWait(int pid) throws InterruptedException {
        long deadline = System.nanoTime() + Duration.ofSeconds(10).toNanos();
        while (System.nanoTime() < deadline) {
            if (Boolean.TRUE.equals(jdbc.queryForObject(
                    "SELECT cardinality(pg_blocking_pids(?)) > 0", Boolean.class, pid))) {
                return;
            }
            Thread.sleep(10);
        }
        throw new AssertionError("Second transaction never waited on a PostgreSQL lock");
    }

    private static void await(CountDownLatch latch) {
        try {
            if (!latch.await(10, TimeUnit.SECONDS)) {
                throw new AssertionError("Timed out awaiting transaction coordination");
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(exception);
        }
    }
}
