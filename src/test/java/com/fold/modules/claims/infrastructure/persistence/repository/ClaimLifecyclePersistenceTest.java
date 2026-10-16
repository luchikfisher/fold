package com.fold.modules.claims.infrastructure.persistence.repository;

import com.fold.kernel.ids.ClaimId;
import com.fold.modules.claims.api.query.GetClaimQuery;
import com.fold.modules.claims.application.feature.lifecycle.ClaimLifecycleHandler;
import com.fold.modules.claims.application.feature.lifecycle.ClaimLifecycleResult;
import com.fold.modules.claims.application.feature.lifecycle.InvalidateClaimCommand;
import com.fold.modules.claims.application.feature.lifecycle.ReinstateClaimCommand;
import com.fold.modules.claims.application.feature.lifecycle.SupersedeClaimCommand;
import com.fold.modules.claims.application.query.GetClaimHandler;
import com.fold.modules.claims.domain.model.Claim;
import com.fold.modules.claims.domain.value.ClaimRevision;
import com.fold.modules.claims.infrastructure.persistence.mapper.ClaimObjectJsonCodec;
import com.fold.modules.claims.infrastructure.persistence.mapper.ClaimPersistenceMapper;
import com.fold.testsupport.claims.ClaimLifecycleIntegrationTestSupport;
import com.fold.testsupport.claims.ClaimTestFixtures;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.transaction.TransactionDefinition;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.spy;

class ClaimLifecyclePersistenceTest extends ClaimLifecycleIntegrationTestSupport {

    @Test
    void readShouldStayAtCapturedHeaderRevisionWhenWriterCommitsBeforeHistoryQuery() throws Exception {
        Claim claim = persistClaim();
        var query = new GetClaimQuery(claim.id());
        var before = queries.get(query).orElseThrow();
        CountDownLatch headerRead = new CountDownLatch(1);
        CountDownLatch readHistory = new CountDownLatch(1);
        NamedParameterJdbcTemplate pausedJdbc = spy(new NamedParameterJdbcTemplate(jdbc));
        doAnswer(invocation -> {
            Object result = invocation.callRealMethod();
            String sql = invocation.getArgument(0);
            if (sql.contains("FROM claims\n")) {
                headerRead.countDown();
                assertThat(readHistory.await(10, TimeUnit.SECONDS)).isTrue();
            }
            return result;
        }).when(pausedJdbc).query(anyString(), any(SqlParameterSource.class), ArgumentMatchers.<RowMapper<Object>>any());
        var reader = new GetClaimHandler(new PostgresClaimRepository(pausedJdbc,
                new ClaimPersistenceMapper(new ClaimObjectJsonCodec(objectMapper))));

        try (var executor = Executors.newSingleThreadExecutor()) {
            var read = executor.submit(() -> reader.get(query).orElseThrow());
            try {
                assertThat(headerRead.await(10, TimeUnit.SECONDS)).isTrue();
                useCase.handle(new InvalidateClaimCommand(claim.id(), INITIAL, REASON, null));
                useCase.handle(new ReinstateClaimCommand(claim.id(), new ClaimRevision(2), REASON, null));
                assertThat(queries.get(query).orElseThrow().revision()).isEqualTo(3);
                readHistory.countDown();
                assertThat(read.get(10, TimeUnit.SECONDS)).isEqualTo(before);
            } finally {
                readHistory.countDown();
            }
        }
    }

    @Test
    void successorCreatedAfterLockPassMustNotBeUsedWithoutHavingBeenLocked() {
        Claim predecessor = persistClaim();
        Claim successor = ClaimTestFixtures.claim();
        PostgresClaimRepository pausedRepository = spy(new PostgresClaimRepository(
                new NamedParameterJdbcTemplate(jdbc),
                new ClaimPersistenceMapper(new ClaimObjectJsonCodec(objectMapper))));
        doAnswer(invocation -> {
            Object locked = invocation.callRealMethod();
            // Commit a new successor on a separate connection after FOR UPDATE
            // found no row for it. READ COMMITTED can now see this unlocked row.
            var creation = tx();
            creation.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
            creation.executeWithoutResult(status -> repository.add(successor));
            return locked;
        }).when(pausedRepository).lockByIds(anyList());
        var handler = new ClaimLifecycleHandler(pausedRepository, () -> NOW, publisher);

        var result = tx().execute(status -> handler.handle(new SupersedeClaimCommand(
                predecessor.id(), INITIAL, successor.id(), null, null)));

        assertThat(result).isEqualTo(new ClaimLifecycleResult.Rejected(
                ClaimLifecycleResult.Rejection.SUCCESSOR_NOT_FOUND));
        assertThat(repository.findById(successor.id())).isPresent();
        assertThat(repository.findById(predecessor.id()).orElseThrow().revision()).isEqualTo(INITIAL);
        assertThat(eventCount()).isZero();
    }

    @Test
    void staleDetachedWriteShouldFailCompareAndSetWithoutAppendingHistory() {
        Claim original = persistClaim();
        Claim stale = repository.findById(original.id()).orElseThrow();
        stale.invalidate(REASON, NOW);
        useCase.handle(new InvalidateClaimCommand(original.id(), INITIAL, REASON, null));

        assertThatThrownBy(() -> tx().executeWithoutResult(status -> {
            repository.lockByIds(List.of(stale.id()));
            repository.appendLifecycleRevision(stale, INITIAL);
        })).isInstanceOf(IllegalStateException.class).hasMessageContaining("revision conflict");
        assertThat(repository.findById(original.id()).orElseThrow().revisionHistory()).hasSize(2);
        assertThat(eventCount()).isEqualTo(1);
    }

    @Test
    void missingSuccessorShouldRollBackHeaderUpdateWhenHistoryInsertFails() {
        Claim original = persistClaim();
        Claim changed = repository.findById(original.id()).orElseThrow();
        changed.supersedeBy(ClaimId.random(), null, NOW);

        assertThatThrownBy(() -> tx().executeWithoutResult(status -> {
            repository.lockByIds(List.of(changed.id()));
            repository.appendLifecycleRevision(changed, INITIAL);
        })).isInstanceOf(DataIntegrityViolationException.class);
        assertThat(repository.findById(original.id()).orElseThrow())
                .usingRecursiveComparison().isEqualTo(original);
        assertThat(eventCount()).isZero();
    }

    @Test
    void lifecycleWritesAndLocksShouldRejectAutocommitUse() {
        Claim claim = persistClaim();
        claim.invalidate(REASON, NOW);
        assertThatThrownBy(() -> repository.lockByIds(List.of(claim.id())))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("active transaction");
        assertThatThrownBy(() -> repository.appendLifecycleRevision(claim, INITIAL))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("active transaction");
        assertThat(repository.findById(claim.id()).orElseThrow().revision()).isEqualTo(INITIAL);
    }

    @Test
    void appendMustNotSkipOrRewriteRevisions() {
        Claim claim = persistClaim();
        claim.invalidate(REASON, NOW);
        claim.reinstate(REASON, NOW);
        assertThatThrownBy(() -> tx().executeWithoutResult(status -> {
            repository.lockByIds(List.of(claim.id()));
            repository.appendLifecycleRevision(claim, INITIAL);
        })).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Exactly one");
        assertThat(repository.findById(claim.id()).orElseThrow().revision()).isEqualTo(INITIAL);
    }

    @Test
    void databaseShouldProtectSuccessorExistenceSelfReferenceAndDeletion() {
        Claim predecessor = persistClaim();
        Claim successor = persistClaim();
        useCase.handle(new SupersedeClaimCommand(predecessor.id(), INITIAL, successor.id(), null, null));

        assertThatThrownBy(() -> jdbc.update("""
                UPDATE claim_lifecycle_revisions SET related_claim_id = ?
                WHERE claim_id = ? AND revision = 2
                """, ClaimId.random().value(), predecessor.id().value()))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("fk_claim_revision_successor");
        assertThatThrownBy(() -> jdbc.update("""
                UPDATE claim_lifecycle_revisions SET related_claim_id = claim_id
                WHERE claim_id = ? AND revision = 2
                """, predecessor.id().value()))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("chk_claim_revision_not_self_successor");
        assertThatThrownBy(() -> jdbc.update("DELETE FROM claims WHERE id = ?", successor.id().value()))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("fk_claim_revision_successor");
        assertThat(repository.findById(predecessor.id()).orElseThrow().revisionHistory().getLast().relatedClaimId())
                .contains(successor.id());
    }
}
