package com.fold.modules.observations.domain.policy;

import com.fold.kernel.ids.ObservationId;
import com.fold.kernel.ids.SourceId;
import com.fold.kernel.time.Timestamp;
import com.fold.modules.observations.domain.model.*;
import com.fold.modules.observations.domain.value.*;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ObservationAcceptancePolicyTest {

    @Test
    void defaultPolicyShouldAcceptStructurallyValidObservation() {
        DefaultObservationAcceptancePolicy policy =
                new DefaultObservationAcceptancePolicy();

        ObservationAcceptanceDecision decision =
                policy.evaluate(observation());

        assertThat(decision.isAccepted()).isTrue();
        assertThat(decision.isRejected()).isFalse();
    }

    private static Observation observation() {
        return Observation.receive(
                ObservationId.random(),
                ObservationType.of("test"),
                ObservationSubject.of(
                        "subject",
                        "subject-1"
                ),
                ObservationPayload.of(
                        ObservationField.of(
                                "name",
                                ObservationValue.Text.of("value")
                        )
                ),
                ObservationOrigin.fromSource(
                        SourceId.random()
                ),
                ObservationFingerprint.sha256V1(
                        "0123456789abcdef".repeat(4)
                ),
                ObservedAt.of(
                        Timestamp.parse(
                                "2026-01-01T00:00:00Z"
                        )
                ),
                ArrivedAt.of(
                        Timestamp.parse(
                                "2026-01-01T00:00:01Z"
                        )
                )
        );
    }
}