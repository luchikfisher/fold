package com.fold.modules.claims.application.feature.lifecycle;

import com.fold.kernel.ids.ClaimId;
import com.fold.modules.claims.domain.value.ClaimLifecycleRevision;
import com.fold.modules.claims.domain.value.ClaimRevision;

import java.util.Objects;

/** Outcomes of a revision-bound lifecycle command. Rejections perform no writes. */
public sealed interface ClaimLifecycleResult {

    record Applied(ClaimId claimId, ClaimLifecycleRevision revision)
            implements ClaimLifecycleResult {
        public Applied {
            Objects.requireNonNull(claimId);
            Objects.requireNonNull(revision);
        }
    }

    /** The original recorded decision, even if the Claim has since changed again. */
    record AlreadyApplied(ClaimId claimId, ClaimLifecycleRevision revision)
            implements ClaimLifecycleResult {
        public AlreadyApplied {
            Objects.requireNonNull(claimId);
            Objects.requireNonNull(revision);
        }
    }

    record NotFound(ClaimId claimId) implements ClaimLifecycleResult {
        public NotFound {
            Objects.requireNonNull(claimId);
        }
    }

    record RevisionConflict(ClaimRevision expected, ClaimRevision actual)
            implements ClaimLifecycleResult {
        public RevisionConflict {
            Objects.requireNonNull(expected);
            Objects.requireNonNull(actual);
        }
    }

    record Rejected(Rejection reason) implements ClaimLifecycleResult {
        public Rejected {
            Objects.requireNonNull(reason);
        }
    }

    enum Rejection {
        INVALID_TRANSITION,
        SUCCESSOR_NOT_FOUND,
        SUCCESSOR_NOT_ACTIVE,
        TIME_REGRESSION
    }
}
