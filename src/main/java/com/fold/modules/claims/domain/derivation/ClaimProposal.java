package com.fold.modules.claims.domain.derivation;

import com.fold.kernel.time.ValidTime;
import com.fold.modules.claims.domain.value.ClaimConfidence;
import com.fold.modules.claims.domain.value.ClaimObservedAt;
import com.fold.modules.claims.domain.value.ClaimOrigin;
import com.fold.modules.claims.domain.value.ClaimProposition;

import java.util.Objects;
import java.util.Optional;

/**
 * Represents a proposed Claim produced by semantic derivation before the Claim
 * becomes authoritative.
 *
 * <p>A proposal has no ClaimId, lifecycle revision, creation timestamp, or
 * persistence identity. Those concepts are assigned only when the proposal is
 * committed as an authoritative Claim.</p>
 *
 * <p>This separation allows semantic derivation to remain deterministic and
 * side-effect free while authoritative Claim creation handles transactional
 * concerns such as identity assignment, idempotency, persistence, and event
 * publication.</p>
 */
public final class ClaimProposal {

    private final ClaimProposition proposition;
    private final ClaimOrigin origin;
    private final ClaimConfidence confidence;
    private final ClaimObservedAt observedAt;
    private final ValidTime validTime;

    public ClaimProposal(
            ClaimProposition proposition,
            ClaimOrigin origin,
            ClaimConfidence confidence,
            ClaimObservedAt observedAt,
            ValidTime validTime
    ) {
        this.proposition = Objects.requireNonNull(
                proposition,
                "proposition must not be null"
        );

        this.origin = Objects.requireNonNull(
                origin,
                "origin must not be null"
        );

        this.confidence = confidence;
        this.observedAt = observedAt;
        this.validTime = validTime;
    }

    public static ClaimProposal of(
            ClaimProposition proposition,
            ClaimOrigin origin
    ) {
        return new ClaimProposal(
                proposition,
                origin,
                null,
                null,
                null
        );
    }

    public ClaimProposition proposition() {
        return proposition;
    }

    public ClaimOrigin origin() {
        return origin;
    }

    public Optional<ClaimConfidence> confidence() {
        return Optional.ofNullable(confidence);
    }

    public Optional<ClaimObservedAt> observedAt() {
        return Optional.ofNullable(observedAt);
    }

    public Optional<ValidTime> validTime() {
        return Optional.ofNullable(validTime);
    }
}