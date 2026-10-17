package com.fold.modules.claims.application.feature.lifecycle;

import com.fold.kernel.events.EventPublisher;
import com.fold.kernel.ids.ClaimId;
import com.fold.kernel.time.FoldClock;
import com.fold.kernel.time.Timestamp;
import com.fold.modules.claims.domain.model.Claim;
import com.fold.modules.claims.domain.repository.ClaimRepository;
import com.fold.modules.claims.domain.value.ClaimLifecycleReason;
import com.fold.modules.claims.domain.value.ClaimRevision;
import com.fold.testsupport.claims.ClaimTestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static com.fold.modules.claims.application.feature.lifecycle.ClaimLifecycleResult.Rejection.TIME_REGRESSION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ClaimLifecycleHandlerTest {
    private static final ClaimLifecycleReason REASON = new ClaimLifecycleReason("RETRACTED", "Source retracted assertion");
    private final ClaimRepository repository = mock(ClaimRepository.class);
    private final FoldClock clock = mock(FoldClock.class);
    private final EventPublisher publisher = mock(EventPublisher.class);
    private final ClaimLifecycleHandler handler = new ClaimLifecycleHandler(repository, clock, publisher);

    @BeforeEach
    void lockExistingTestClaims() {
        when(repository.lockByIds(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void regressingClockShouldRejectWithoutMutatingOrPersistingClaim() {
        Claim claim = ClaimTestFixtures.claim();
        when(repository.findById(claim.id())).thenReturn(Optional.of(claim));
        when(clock.now()).thenReturn(Timestamp.parse("2026-10-02T10:00:00Z"));

        assertThat(handler.handle(new InvalidateClaimCommand(claim.id(), ClaimRevision.initial(), REASON, null)))
                .isEqualTo(new ClaimLifecycleResult.Rejected(TIME_REGRESSION));
        assertThat(claim.revision()).isEqualTo(ClaimRevision.initial());
        verify(repository, never()).appendLifecycleRevision(any(), any());
        verifyNoInteractions(publisher);
    }

    @Test
    void supersessionMustNotPredateSuccessorsLatestActiveRevision() {
        Claim predecessor = ClaimTestFixtures.claim();
        Claim successor = ClaimTestFixtures.claim();
        successor.invalidate(REASON, Timestamp.parse("2026-10-04T10:00:00Z"));
        successor.reinstate(REASON, Timestamp.parse("2026-10-05T10:00:00Z"));
        when(repository.findById(predecessor.id())).thenReturn(Optional.of(predecessor));
        when(repository.findById(successor.id())).thenReturn(Optional.of(successor));
        when(clock.now()).thenReturn(Timestamp.parse("2026-10-04T10:00:00Z"));

        assertThat(handler.handle(new SupersedeClaimCommand(predecessor.id(), ClaimRevision.initial(),
                successor.id(), null, null))).isEqualTo(new ClaimLifecycleResult.Rejected(TIME_REGRESSION));
        verify(repository, never()).appendLifecycleRevision(any(), any());
        verifyNoInteractions(publisher);
    }

    @Test
    void historicalReplayShouldNotReadClockOrWriteAnything() {
        Claim claim = ClaimTestFixtures.claim();
        claim.invalidate(REASON, ClaimTestFixtures.CREATED_AT);
        var original = claim.revisionHistory().getLast();
        claim.reinstate(REASON, ClaimTestFixtures.CREATED_AT);
        when(repository.findById(claim.id())).thenReturn(Optional.of(claim));

        assertThat(handler.handle(new InvalidateClaimCommand(claim.id(), ClaimRevision.initial(), REASON, null)))
                .isEqualTo(new ClaimLifecycleResult.AlreadyApplied(claim.id(), original));
        verify(repository, never()).appendLifecycleRevision(any(), any());
        verifyNoInteractions(clock, publisher);
    }

    @Test
    void futureExpectedRevisionShouldConflictWithoutOverflowOrSideEffects() {
        Claim claim = ClaimTestFixtures.claim();
        when(repository.findById(claim.id())).thenReturn(Optional.of(claim));
        var future = new ClaimRevision(Long.MAX_VALUE);
        assertThat(handler.handle(new InvalidateClaimCommand(claim.id(), future, REASON, null)))
                .isEqualTo(new ClaimLifecycleResult.RevisionConflict(future, ClaimRevision.initial()));
        verifyNoInteractions(clock, publisher);
    }

    @Test
    void commandsShouldRequireIdentityRevisionAndReasonsWhereRequired() {
        ClaimId id = ClaimId.random();
        ClaimRevision revision = ClaimRevision.initial();
        assertThatThrownBy(() -> new InvalidateClaimCommand(null, revision, REASON, null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new InvalidateClaimCommand(id, null, REASON, null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new InvalidateClaimCommand(id, revision, null, null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new ReinstateClaimCommand(id, revision, null, null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new SupersedeClaimCommand(id, revision, null, null, null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new SupersedeClaimCommand(id, revision, id, null, null))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("itself");
        assertThat(new SupersedeClaimCommand(id, revision, ClaimId.random(), null, null).reason()).isNull();
    }
}
