package com.fold.modules.observations.domain.event;

import com.fold.kernel.ids.ObservationId;
import com.fold.kernel.time.Timestamp;
import com.fold.modules.observations.domain.value.ObservationRejection;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ObservationDomainEventTest {

    private static final Timestamp OCCURRED_AT =
            Timestamp.parse("2026-09-27T10:00:00Z");

    @Test
    void acceptanceEventShouldDescribeAcceptedObservation() {
        ObservationId observationId =
                ObservationId.random();

        ObservationAccepted event =
                new ObservationAccepted(
                        observationId,
                        OCCURRED_AT
                );

        assertThat(event.observationId())
                .isEqualTo(observationId);

        assertThat(event.occurredAt())
                .isEqualTo(OCCURRED_AT);
    }

    @Test
    void rejectionEventShouldCarryRejectionReason() {
        ObservationId observationId =
                ObservationId.random();

        ObservationRejection rejection =
                ObservationRejection.of(
                        "invalid",
                        "Observation is invalid"
                );

        ObservationRejected event =
                new ObservationRejected(
                        observationId,
                        rejection,
                        OCCURRED_AT
                );

        assertThat(event.observationId())
                .isEqualTo(observationId);

        assertThat(event.rejection())
                .isEqualTo(rejection);

        assertThat(event.occurredAt())
                .isEqualTo(OCCURRED_AT);
    }

    @Test
    void acceptanceEventShouldRequireObservationId() {
        assertThatThrownBy(
                () -> new ObservationAccepted(
                        null,
                        OCCURRED_AT
                )
        )
                .isInstanceOf(
                        NullPointerException.class
                );
    }

    @Test
    void rejectionEventShouldRequireRejectionReason() {
        assertThatThrownBy(
                () -> new ObservationRejected(
                        ObservationId.random(),
                        null,
                        OCCURRED_AT
                )
        )
                .isInstanceOf(
                        NullPointerException.class
                );
    }
}