package com.fold.modules.observations.domain.model;

import com.fold.kernel.ids.EvidenceId;
import com.fold.kernel.ids.ObservationId;
import com.fold.kernel.ids.SourceId;
import com.fold.kernel.result.DomainError;
import com.fold.kernel.result.Result;
import com.fold.kernel.time.Timestamp;
import com.fold.modules.observations.domain.error.ObservationTransitionError;
import com.fold.modules.observations.domain.event.ObservationAccepted;
import com.fold.modules.observations.domain.event.ObservationRejected;
import com.fold.modules.observations.domain.value.*;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ObservationTest {

    private static final Timestamp OBSERVED =
            Timestamp.parse("2026-09-27T10:00:00Z");

    private static final Timestamp ARRIVED =
            Timestamp.parse("2026-09-27T10:05:00Z");

    private static final Timestamp DECIDED =
            Timestamp.parse("2026-09-27T10:05:01Z");

    private static final String FINGERPRINT =
            "0123456789abcdef"
                    + "0123456789abcdef"
                    + "0123456789abcdef"
                    + "0123456789abcdef";

    @Test
    void newlyReceivedObservationShouldBeginInReceivedState() {
        Observation observation = newObservation();

        assertThat(observation.status())
                .isEqualTo(ObservationStatus.RECEIVED);

        assertThat(observation.isReceived()).isTrue();
        assertThat(observation.isAccepted()).isFalse();
        assertThat(observation.isRejected()).isFalse();
        assertThat(observation.isDecided()).isFalse();

        assertThat(observation.decidedAt()).isEmpty();
        assertThat(observation.rejection()).isEmpty();
    }

    @Test
    void receivedObservationShouldPreserveItsSemanticContent() {
        ObservationId id = ObservationId.random();

        Observation observation = newObservation(id);

        assertThat(observation.id()).isEqualTo(id);

        assertThat(observation.type())
                .isEqualTo(
                        ObservationType.of(
                                "organization-registration"
                        )
                );

        assertThat(observation.subject())
                .isEqualTo(
                        ObservationSubject.of(
                                "organization",
                                "registry:company:123"
                        )
                );

        assertThat(observation.payload().find("organization.name"))
                .contains(
                        ObservationField.of(
                                "organization.name",
                                ObservationValue.Text.of(
                                        "Acme Ltd"
                                )
                        )
                );

        assertThat(observation.origin().sourceId())
                .isNotNull();

        assertThat(observation.origin().evidence())
                .isPresent();

        assertThat(observation.fingerprint().isSha256())
                .isTrue();

        assertThat(observation.observedAt())
                .isEqualTo(ObservedAt.of(OBSERVED));

        assertThat(observation.arrivedAt())
                .isEqualTo(ArrivedAt.of(ARRIVED));
    }

    @Test
    void acceptingReceivedObservationShouldTransitionToAccepted() {
        Observation observation = newObservation();

        Result<ObservationAccepted> result =
                observation.accept(DECIDED);

        assertThat(result.isSuccess()).isTrue();

        assertThat(observation.status())
                .isEqualTo(ObservationStatus.ACCEPTED);

        assertThat(observation.isAccepted()).isTrue();
        assertThat(observation.isReceived()).isFalse();
        assertThat(observation.isRejected()).isFalse();
        assertThat(observation.isDecided()).isTrue();

        assertThat(observation.decidedAt())
                .contains(DECIDED);

        assertThat(observation.rejection())
                .isEmpty();
    }

    @Test
    void acceptingObservationShouldProduceAcceptanceEvent() {
        Observation observation = newObservation();

        ObservationAccepted event =
                observation.accept(DECIDED)
                        .fold(
                                value -> value,
                                error -> {
                                    throw new AssertionError(error.message());
                                }
                        );

        assertThat(event.observationId())
                .isEqualTo(observation.id());

        assertThat(event.occurredAt())
                .isEqualTo(DECIDED);
    }

    @Test
    void rejectingReceivedObservationShouldTransitionToRejected() {
        Observation observation = newObservation();

        ObservationRejection rejection =
                ObservationRejection.of(
                        "invalid-semantic-input",
                        "The normalized input is not admissible"
                );

        Result<ObservationRejected> result =
                observation.reject(
                        rejection,
                        DECIDED
                );

        assertThat(result.isSuccess()).isTrue();

        assertThat(observation.status())
                .isEqualTo(ObservationStatus.REJECTED);

        assertThat(observation.isRejected()).isTrue();
        assertThat(observation.isReceived()).isFalse();
        assertThat(observation.isAccepted()).isFalse();
        assertThat(observation.isDecided()).isTrue();

        assertThat(observation.decidedAt())
                .contains(DECIDED);

        assertThat(observation.rejection())
                .contains(rejection);
    }

    @Test
    void rejectingObservationShouldProduceRejectionEvent() {
        Observation observation = newObservation();

        ObservationRejection rejection =
                ObservationRejection.of(
                        "missing-required-semantics",
                        "Required semantic information is missing"
                );

        ObservationRejected event =
                observation.reject(
                                rejection,
                                DECIDED
                        )
                        .fold(
                                value -> value,
                                error -> {
                                    throw new AssertionError(error.message());
                                }
                        );

        assertThat(event.observationId())
                .isEqualTo(observation.id());

        assertThat(event.rejection())
                .isEqualTo(rejection);

        assertThat(event.occurredAt())
                .isEqualTo(DECIDED);
    }

    @Test
    void acceptedObservationMustNotBeAcceptedAgain() {
        Observation observation = newObservation();

        observation.accept(DECIDED);

        Result<ObservationAccepted> second =
                observation.accept(
                        Timestamp.parse(
                                "2026-09-27T10:06:00Z"
                        )
                );

        assertThat(second.isFailure()).isTrue();

        DomainError error =
                second.fold(
                        value -> null,
                        failure -> failure
                );

        assertThat(error)
                .isInstanceOf(
                        ObservationTransitionError.class
                );

        ObservationTransitionError transitionError =
                (ObservationTransitionError) error;

        assertThat(transitionError.currentStatus())
                .isEqualTo(ObservationStatus.ACCEPTED);

        assertThat(transitionError.targetStatus())
                .isEqualTo(ObservationStatus.ACCEPTED);

        assertThat(observation.status())
                .isEqualTo(ObservationStatus.ACCEPTED);

        assertThat(observation.decidedAt())
                .contains(DECIDED);
    }

    @Test
    void acceptedObservationMustNotBeRejected() {
        Observation observation = newObservation();

        observation.accept(DECIDED);

        Result<ObservationRejected> result =
                observation.reject(
                        ObservationRejection.of(
                                "later-rejection",
                                "This transition must not be allowed"
                        ),
                        Timestamp.parse(
                                "2026-09-27T10:06:00Z"
                        )
                );

        assertThat(result.isFailure()).isTrue();

        assertThat(observation.status())
                .isEqualTo(ObservationStatus.ACCEPTED);

        assertThat(observation.rejection())
                .isEmpty();
    }

    @Test
    void rejectedObservationMustNotBeRejectedAgain() {
        Observation observation = newObservation();

        ObservationRejection original =
                ObservationRejection.of(
                        "original",
                        "Original rejection"
                );

        observation.reject(
                original,
                DECIDED
        );

        Result<ObservationRejected> second =
                observation.reject(
                        ObservationRejection.of(
                                "replacement",
                                "Replacement rejection"
                        ),
                        Timestamp.parse(
                                "2026-09-27T10:06:00Z"
                        )
                );

        assertThat(second.isFailure()).isTrue();

        assertThat(observation.rejection())
                .contains(original);

        assertThat(observation.decidedAt())
                .contains(DECIDED);
    }

    @Test
    void rejectedObservationMustNotBeAccepted() {
        Observation observation = newObservation();

        observation.reject(
                ObservationRejection.of(
                        "invalid",
                        "Observation was rejected"
                ),
                DECIDED
        );

        Result<ObservationAccepted> result =
                observation.accept(
                        Timestamp.parse(
                                "2026-09-27T10:06:00Z"
                        )
                );

        assertThat(result.isFailure()).isTrue();

        assertThat(observation.status())
                .isEqualTo(ObservationStatus.REJECTED);
    }

    @Test
    void decisionMayOccurAtExactArrivalTime() {
        Observation observation = newObservation();

        Result<ObservationAccepted> result =
                observation.accept(ARRIVED);

        assertThat(result.isSuccess()).isTrue();

        assertThat(observation.decidedAt())
                .contains(ARRIVED);
    }

    @Test
    void decisionMustNotPrecedeArrival() {
        Observation observation = newObservation();

        Timestamp beforeArrival =
                Timestamp.parse(
                        "2026-09-27T10:04:59Z"
                );

        assertThatThrownBy(
                () -> observation.accept(beforeArrival)
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "decidedAt must not be before arrivedAt"
                );

        assertThat(observation.status())
                .isEqualTo(ObservationStatus.RECEIVED);

        assertThat(observation.decidedAt())
                .isEmpty();
    }

    @Test
    void failedRejectionDecisionMustLeaveAggregateUnchanged() {
        Observation observation = newObservation();

        Timestamp beforeArrival =
                Timestamp.parse(
                        "2026-09-27T10:04:59Z"
                );

        assertThatThrownBy(
                () -> observation.reject(
                        ObservationRejection.of(
                                "invalid",
                                "Invalid observation"
                        ),
                        beforeArrival
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                );

        assertThat(observation.status())
                .isEqualTo(ObservationStatus.RECEIVED);

        assertThat(observation.decidedAt())
                .isEmpty();

        assertThat(observation.rejection())
                .isEmpty();
    }

    @Test
    void observedTimeMayBeAfterArrivalTime() {
        Observation observation =
                Observation.receive(
                        ObservationId.random(),
                        ObservationType.of("scheduled-event"),
                        ObservationSubject.of(
                                "event",
                                "source:event:123"
                        ),
                        ObservationPayload.of(
                                ObservationField.of(
                                        "event.name",
                                        ObservationValue.Text.of(
                                                "Future Event"
                                        )
                                )
                        ),
                        ObservationOrigin.fromSource(
                                SourceId.random()
                        ),
                        ObservationFingerprint.sha256V1(
                                FINGERPRINT
                        ),
                        ObservedAt.of(
                                Timestamp.parse(
                                        "2027-01-01T00:00:00Z"
                                )
                        ),
                        ArrivedAt.of(
                                Timestamp.parse(
                                        "2026-09-27T10:00:00Z"
                                )
                        )
                );

        assertThat(
                observation.observedAt()
                        .value()
                        .isAfter(
                                observation.arrivedAt().value()
                        )
        ).isTrue();
    }

    @Test
    void restoringReceivedObservationShouldRequireNoDecisionState() {
        Observation observation =
                Observation.restore(
                        ObservationId.random(),
                        ObservationType.of(
                                "organization-registration"
                        ),
                        defaultSubject(),
                        defaultPayload(),
                        defaultOrigin(),
                        defaultFingerprint(),
                        ObservedAt.of(OBSERVED),
                        ArrivedAt.of(ARRIVED),
                        ObservationStatus.RECEIVED,
                        null,
                        null
                );

        assertThat(observation.isReceived()).isTrue();
    }

    @Test
    void restoringAcceptedObservationShouldRequireDecisionTime() {
        assertThatThrownBy(
                () -> Observation.restore(
                        ObservationId.random(),
                        ObservationType.of(
                                "organization-registration"
                        ),
                        defaultSubject(),
                        defaultPayload(),
                        defaultOrigin(),
                        defaultFingerprint(),
                        ObservedAt.of(OBSERVED),
                        ArrivedAt.of(ARRIVED),
                        ObservationStatus.ACCEPTED,
                        null,
                        null
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "accepted observation must have decidedAt"
                );
    }

    @Test
    void restoredAcceptedObservationMustNotContainRejection() {
        assertThatThrownBy(
                () -> Observation.restore(
                        ObservationId.random(),
                        ObservationType.of(
                                "organization-registration"
                        ),
                        defaultSubject(),
                        defaultPayload(),
                        defaultOrigin(),
                        defaultFingerprint(),
                        ObservedAt.of(OBSERVED),
                        ArrivedAt.of(ARRIVED),
                        ObservationStatus.ACCEPTED,
                        DECIDED,
                        ObservationRejection.of(
                                "invalid",
                                "Should not exist"
                        )
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "accepted observation must not have rejection"
                );
    }

    @Test
    void restoringRejectedObservationShouldRequireRejectionReason() {
        assertThatThrownBy(
                () -> Observation.restore(
                        ObservationId.random(),
                        ObservationType.of(
                                "organization-registration"
                        ),
                        defaultSubject(),
                        defaultPayload(),
                        defaultOrigin(),
                        defaultFingerprint(),
                        ObservedAt.of(OBSERVED),
                        ArrivedAt.of(ARRIVED),
                        ObservationStatus.REJECTED,
                        DECIDED,
                        null
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "rejected observation must have rejection"
                );
    }

    @Test
    void restoringRejectedObservationShouldRequireDecisionTime() {
        assertThatThrownBy(
                () -> Observation.restore(
                        ObservationId.random(),
                        ObservationType.of(
                                "organization-registration"
                        ),
                        defaultSubject(),
                        defaultPayload(),
                        defaultOrigin(),
                        defaultFingerprint(),
                        ObservedAt.of(OBSERVED),
                        ArrivedAt.of(ARRIVED),
                        ObservationStatus.REJECTED,
                        null,
                        ObservationRejection.of(
                                "invalid",
                                "Rejected observation"
                        )
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "rejected observation must have decidedAt"
                );
    }

    @Test
    void restoredDecisionMustNotPrecedeArrival() {
        assertThatThrownBy(
                () -> Observation.restore(
                        ObservationId.random(),
                        ObservationType.of(
                                "organization-registration"
                        ),
                        defaultSubject(),
                        defaultPayload(),
                        defaultOrigin(),
                        defaultFingerprint(),
                        ObservedAt.of(OBSERVED),
                        ArrivedAt.of(ARRIVED),
                        ObservationStatus.ACCEPTED,
                        Timestamp.parse(
                                "2026-09-27T10:04:59Z"
                        ),
                        null
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "decidedAt must not be before arrivedAt"
                );
    }

    @Test
    void observationsWithSameIdentityShouldBeEqual() {
        ObservationId id = ObservationId.random();

        Observation first = newObservation(id);
        Observation second = newObservation(id);

        assertThat(first)
                .isEqualTo(second);

        assertThat(first.hashCode())
                .isEqualTo(second.hashCode());
    }

    @Test
    void observationsWithDifferentIdentitiesShouldNotBeEqual() {
        assertThat(newObservation())
                .isNotEqualTo(newObservation());
    }

    @Test
    void lifecycleChangeMustNotChangeAggregateIdentity() {
        Observation observation = newObservation();

        int hashBefore = observation.hashCode();

        observation.accept(DECIDED);

        assertThat(observation.hashCode())
                .isEqualTo(hashBefore);
    }

    private static Observation newObservation() {
        return newObservation(
                ObservationId.random()
        );
    }

    private static Observation newObservation(
            ObservationId id
    ) {
        return Observation.receive(
                id,
                ObservationType.of(
                        "organization-registration"
                ),
                defaultSubject(),
                defaultPayload(),
                defaultOrigin(),
                defaultFingerprint(),
                ObservedAt.of(OBSERVED),
                ArrivedAt.of(ARRIVED)
        );
    }

    private static ObservationSubject defaultSubject() {
        return ObservationSubject.of(
                "organization",
                "registry:company:123"
        );
    }

    private static ObservationPayload defaultPayload() {
        return ObservationPayload.of(
                ObservationField.of(
                        "organization.name",
                        ObservationValue.Text.of(
                                "Acme Ltd"
                        )
                )
        );
    }

    private static ObservationOrigin defaultOrigin() {
        return ObservationOrigin.fromRecordEvidence(
                SourceId.random(),
                EvidenceId.random(),
                "record-123"
        );
    }

    private static ObservationFingerprint defaultFingerprint() {
        return ObservationFingerprint.sha256V1(
                FINGERPRINT
        );
    }
}