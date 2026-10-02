package com.fold.modules.observations.domain.model;

import com.fold.kernel.ids.ObservationId;
import com.fold.kernel.result.Result;
import com.fold.kernel.time.Timestamp;
import com.fold.modules.observations.domain.error.ObservationTransitionError;
import com.fold.modules.observations.domain.event.ObservationAccepted;
import com.fold.modules.observations.domain.event.ObservationRejected;
import com.fold.modules.observations.domain.value.*;

import java.util.Objects;
import java.util.Optional;

/**
 * Represents one immutable piece of normalized information received by FOLD
 * together with its admission state.
 *
 * <p>An observation records what was received before FOLD resolves identity,
 * creates claims, calculates beliefs, detects conflicts, or infers
 * relationships. It therefore describes input to the knowledge model rather
 * than a conclusion about the modeled world.</p>
 *
 * <p>The semantic content of an observation never changes. Its only mutable
 * domain state is its admission lifecycle:</p>
 *
 * <pre>
 * RECEIVED ──► ACCEPTED
 *     │
 *     └──────► REJECTED
 * </pre>
 *
 * <p>{@code ACCEPTED} and {@code REJECTED} are terminal states. An observation
 * cannot be accepted after rejection, rejected after acceptance, or decided
 * more than once.</p>
 *
 * <p>A rejected observation remains authoritative history. Rejection prevents
 * downstream knowledge processing; it does not delete or rewrite the received
 * information.</p>
 *
 * <p>Equality is based solely on {@link ObservationId}. Two aggregate
 * instances representing the same observation identity are therefore equal
 * regardless of their currently loaded lifecycle state.</p>
 */
public final class Observation {

    private final ObservationId id;
    private final ObservationType type;
    private final ObservationSubject subject;
    private final ObservationPayload payload;
    private final ObservationOrigin origin;
    private final ObservationFingerprint fingerprint;
    private final ObservedAt observedAt;
    private final ArrivedAt arrivedAt;

    private ObservationStatus status;
    private Timestamp decidedAt;
    private ObservationRejection rejection;

    private Observation(
            ObservationId id,
            ObservationType type,
            ObservationSubject subject,
            ObservationPayload payload,
            ObservationOrigin origin,
            ObservationFingerprint fingerprint,
            ObservedAt observedAt,
            ArrivedAt arrivedAt,
            ObservationStatus status,
            Timestamp decidedAt,
            ObservationRejection rejection
    ) {
        this.id = Objects.requireNonNull(
                id,
                "id must not be null"
        );
        this.type = Objects.requireNonNull(
                type,
                "type must not be null"
        );
        this.subject = Objects.requireNonNull(
                subject,
                "subject must not be null"
        );
        this.payload = Objects.requireNonNull(
                payload,
                "payload must not be null"
        );
        this.origin = Objects.requireNonNull(
                origin,
                "origin must not be null"
        );
        this.fingerprint = Objects.requireNonNull(
                fingerprint,
                "fingerprint must not be null"
        );
        this.observedAt = Objects.requireNonNull(
                observedAt,
                "observedAt must not be null"
        );
        this.arrivedAt = Objects.requireNonNull(
                arrivedAt,
                "arrivedAt must not be null"
        );
        this.status = Objects.requireNonNull(
                status,
                "status must not be null"
        );

        validateLifecycleState(
                status,
                decidedAt,
                rejection
        );

        if (decidedAt != null
                && decidedAt.isBefore(arrivedAt.value())) {
            throw new IllegalArgumentException(
                    "decidedAt must not be before arrivedAt"
            );
        }

        this.decidedAt = decidedAt;
        this.rejection = rejection;
    }

    /**
     * Creates a newly received observation.
     *
     * <p>A newly received observation always begins in
     * {@link ObservationStatus#RECEIVED}. No admission decision exists yet.</p>
     *
     * @param id          the observation identity
     * @param type        the semantic observation type
     * @param subject     the source-facing semantic subject
     * @param payload     the normalized semantic payload
     * @param origin      the observation origin
     * @param fingerprint the deterministic content fingerprint
     * @param observedAt  when the information was observed according to its source
     * @param arrivedAt   when FOLD received the observation
     * @return the received observation
     */
    public static Observation receive(
            ObservationId id,
            ObservationType type,
            ObservationSubject subject,
            ObservationPayload payload,
            ObservationOrigin origin,
            ObservationFingerprint fingerprint,
            ObservedAt observedAt,
            ArrivedAt arrivedAt
    ) {
        return new Observation(
                id,
                type,
                subject,
                payload,
                origin,
                fingerprint,
                observedAt,
                arrivedAt,
                ObservationStatus.RECEIVED,
                null,
                null
        );
    }

    /**
     * Restores a previously persisted observation without performing a new
     * lifecycle transition or producing a domain event.
     *
     * <p>This factory exists for persistence reconstruction. The restored state
     * is validated against the same invariants as a live aggregate.</p>
     *
     * @param id          the observation identity
     * @param type        the semantic observation type
     * @param subject     the source-facing semantic subject
     * @param payload     the normalized semantic payload
     * @param origin      the observation origin
     * @param fingerprint the deterministic content fingerprint
     * @param observedAt  the source-facing observation time
     * @param arrivedAt   the FOLD arrival time
     * @param status      the persisted lifecycle status
     * @param decidedAt   the admission-decision time, if a decision exists
     * @param rejection   the rejection reason, if rejected
     * @return the restored observation
     */
    public static Observation restore(
            ObservationId id,
            ObservationType type,
            ObservationSubject subject,
            ObservationPayload payload,
            ObservationOrigin origin,
            ObservationFingerprint fingerprint,
            ObservedAt observedAt,
            ArrivedAt arrivedAt,
            ObservationStatus status,
            Timestamp decidedAt,
            ObservationRejection rejection
    ) {
        return new Observation(
                id,
                type,
                subject,
                payload,
                origin,
                fingerprint,
                observedAt,
                arrivedAt,
                status,
                decidedAt,
                rejection
        );
    }

    /**
     * Accepts this observation into downstream knowledge processing.
     *
     * <p>Acceptance is allowed only while the observation is
     * {@link ObservationStatus#RECEIVED}. A successful transition is terminal
     * and produces an {@link ObservationAccepted} domain event.</p>
     *
     * @param decidedAt when the acceptance decision occurred
     * @return the acceptance event, or an expected transition error when this
     * observation has already reached a terminal state
     * @throws NullPointerException     if {@code decidedAt} is null
     * @throws IllegalArgumentException if the decision precedes arrival
     */
    public Result<ObservationAccepted> accept(
            Timestamp decidedAt
    ) {
        requireValidDecisionTime(decidedAt);

        if (status != ObservationStatus.RECEIVED) {
            return Result.failure(
                    new ObservationTransitionError(
                            id,
                            status,
                            ObservationStatus.ACCEPTED
                    )
            );
        }

        status = ObservationStatus.ACCEPTED;
        this.decidedAt = decidedAt;
        rejection = null;

        return Result.success(
                new ObservationAccepted(
                        id,
                        decidedAt
                )
        );
    }

    /**
     * Rejects this observation from downstream knowledge processing.
     *
     * <p>Rejection is allowed only while the observation is
     * {@link ObservationStatus#RECEIVED}. The supplied rejection reason becomes
     * part of the authoritative observation state. A successful transition is
     * terminal and produces an {@link ObservationRejected} domain event.</p>
     *
     * @param rejection the reason for rejection
     * @param decidedAt when the rejection decision occurred
     * @return the rejection event, or an expected transition error when this
     * observation has already reached a terminal state
     * @throws NullPointerException     if either argument is null
     * @throws IllegalArgumentException if the decision precedes arrival
     */
    public Result<ObservationRejected> reject(
            ObservationRejection rejection,
            Timestamp decidedAt
    ) {
        Objects.requireNonNull(
                rejection,
                "rejection must not be null"
        );

        requireValidDecisionTime(decidedAt);

        if (status != ObservationStatus.RECEIVED) {
            return Result.failure(
                    new ObservationTransitionError(
                            id,
                            status,
                            ObservationStatus.REJECTED
                    )
            );
        }

        status = ObservationStatus.REJECTED;
        this.decidedAt = decidedAt;
        this.rejection = rejection;

        return Result.success(
                new ObservationRejected(
                        id,
                        rejection,
                        decidedAt
                )
        );
    }

    /**
     * Returns whether this observation is still awaiting an admission decision.
     *
     * @return {@code true} when the status is {@code RECEIVED}
     */
    public boolean isReceived() {
        return status == ObservationStatus.RECEIVED;
    }

    /**
     * Returns whether this observation was accepted.
     *
     * @return {@code true} when the status is {@code ACCEPTED}
     */
    public boolean isAccepted() {
        return status == ObservationStatus.ACCEPTED;
    }

    /**
     * Returns whether this observation was rejected.
     *
     * @return {@code true} when the status is {@code REJECTED}
     */
    public boolean isRejected() {
        return status == ObservationStatus.REJECTED;
    }

    /**
     * Returns whether this observation has reached a terminal lifecycle state.
     *
     * @return {@code true} when accepted or rejected
     */
    public boolean isDecided() {
        return status != ObservationStatus.RECEIVED;
    }

    public ObservationId id() {
        return id;
    }

    public ObservationType type() {
        return type;
    }

    public ObservationSubject subject() {
        return subject;
    }

    public ObservationPayload payload() {
        return payload;
    }

    public ObservationOrigin origin() {
        return origin;
    }

    public ObservationFingerprint fingerprint() {
        return fingerprint;
    }

    public ObservedAt observedAt() {
        return observedAt;
    }

    public ArrivedAt arrivedAt() {
        return arrivedAt;
    }

    public ObservationStatus status() {
        return status;
    }

    /**
     * Returns when the observation was accepted or rejected.
     *
     * @return the decision time, or empty while still received
     */
    public Optional<Timestamp> decidedAt() {
        return Optional.ofNullable(decidedAt);
    }

    /**
     * Returns the rejection reason when the observation was rejected.
     *
     * @return the rejection reason, or empty for received and accepted observations
     */
    public Optional<ObservationRejection> rejection() {
        return Optional.ofNullable(rejection);
    }

    private void requireValidDecisionTime(
            Timestamp decidedAt
    ) {
        Objects.requireNonNull(
                decidedAt,
                "decidedAt must not be null"
        );

        if (decidedAt.isBefore(arrivedAt.value())) {
            throw new IllegalArgumentException(
                    "decidedAt must not be before arrivedAt"
            );
        }
    }

    private static void validateLifecycleState(
            ObservationStatus status,
            Timestamp decidedAt,
            ObservationRejection rejection
    ) {
        switch (status) {
            case RECEIVED -> {
                if (decidedAt != null) {
                    throw new IllegalArgumentException(
                            "received observation must not have decidedAt"
                    );
                }

                if (rejection != null) {
                    throw new IllegalArgumentException(
                            "received observation must not have rejection"
                    );
                }
            }

            case ACCEPTED -> {
                if (decidedAt == null) {
                    throw new IllegalArgumentException(
                            "accepted observation must have decidedAt"
                    );
                }

                if (rejection != null) {
                    throw new IllegalArgumentException(
                            "accepted observation must not have rejection"
                    );
                }
            }

            case REJECTED -> {
                if (decidedAt == null) {
                    throw new IllegalArgumentException(
                            "rejected observation must have decidedAt"
                    );
                }

                if (rejection == null) {
                    throw new IllegalArgumentException(
                            "rejected observation must have rejection"
                    );
                }
            }
        }
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof Observation observation)) {
            return false;
        }

        return id.equals(observation.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Observation[id=%s, type=%s, status=%s]"
                .formatted(
                        id,
                        type,
                        status
                );
    }
}