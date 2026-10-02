package com.fold.modules.observations.api.event;

import com.fold.kernel.events.EventVersion;
import com.fold.kernel.ids.ObservationId;
import com.fold.kernel.time.Timestamp;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ObservationIntegrationEventTest {

    private static final Timestamp NOW =
            Timestamp.parse("2026-01-01T00:00:00Z");

    @Test
    void acceptedEventShouldExposeStableContract() {
        ObservationAcceptedV1 event =
                new ObservationAcceptedV1(
                        ObservationId.random(),
                        NOW
                );

        assertThat(event.eventType())
                .isEqualTo(
                        "observations.observation-accepted"
                );

        assertThat(event.version())
                .isEqualTo(EventVersion.initial());

        assertThat(event.occurredAt())
                .isEqualTo(NOW);
    }

    @Test
    void rejectedEventShouldExposeRejection() {
        ObservationRejectedV1 event =
                new ObservationRejectedV1(
                        ObservationId.random(),
                        "invalid",
                        "Observation is invalid",
                        NOW
                );

        assertThat(event.eventType())
                .isEqualTo(
                        "observations.observation-rejected"
                );

        assertThat(event.rejectionCode())
                .isEqualTo("invalid");

        assertThat(event.rejectionMessage())
                .isEqualTo(
                        "Observation is invalid"
                );
    }

    @Test
    void duplicateEventShouldIdentifyExistingObservation() {
        ObservationId existing =
                ObservationId.random();

        DuplicateObservationDetectedV1 event =
                new DuplicateObservationDetectedV1(
                        existing,
                        NOW
                );

        assertThat(event.eventType())
                .isEqualTo(
                        "observations.duplicate-observation-detected"
                );

        assertThat(event.existingObservationId())
                .isEqualTo(existing);
    }
}