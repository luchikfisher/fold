package com.fold.modules.claims.api.view;

import com.fold.kernel.ids.ClaimId;
import com.fold.kernel.time.Timestamp;

import java.util.List;
import java.util.Objects;

/**
 * Public read representation of one authoritative Claim.
 */
public record ClaimView(
        ClaimId id,
        Subject subject,
        String predicate,
        ClaimObjectView object,
        List<ClaimOriginReferenceView> origin,
        Derivation derivation,
        Double confidence,
        Timestamp observedAt,
        Validity validTime,
        Timestamp createdAt,
        String status,
        long revision,
        List<LifecycleRevision> lifecycle
) {

    public ClaimView {
        Objects.requireNonNull(id);
        Objects.requireNonNull(subject);
        Objects.requireNonNull(predicate);
        Objects.requireNonNull(object);
        Objects.requireNonNull(origin);
        Objects.requireNonNull(derivation);
        Objects.requireNonNull(createdAt);
        Objects.requireNonNull(status);
        Objects.requireNonNull(lifecycle);

        origin = List.copyOf(origin);
        lifecycle = List.copyOf(lifecycle);
    }

    public record Subject(
            String namespace,
            String value
    ) {
        public Subject {
            Objects.requireNonNull(namespace);
            Objects.requireNonNull(value);
        }
    }

    public record Derivation(
            String kind,
            String method,
            int version
    ) {
        public Derivation {
            Objects.requireNonNull(kind);
            Objects.requireNonNull(method);
        }
    }

    public record Validity(
            Timestamp startInclusive,
            Timestamp endExclusive
    ) {
        public Validity {
            Objects.requireNonNull(startInclusive);
        }
    }

    public record LifecycleRevision(
            long revision,
            String transition,
            String status,
            Timestamp changedAt,
            ClaimId relatedClaimId,
            Reason reason
    ) {
        public LifecycleRevision {
            Objects.requireNonNull(transition);
            Objects.requireNonNull(status);
            Objects.requireNonNull(changedAt);
        }
    }

    public record Reason(
            String code,
            String message
    ) {
        public Reason {
            Objects.requireNonNull(code);
            Objects.requireNonNull(message);
        }
    }
}