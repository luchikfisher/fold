package com.fold.modules.observations.domain.error;

import com.fold.kernel.ids.ObservationId;
import com.fold.modules.observations.domain.value.ObservationStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ObservationTransitionErrorTest {

    @Test
    void shouldExposeStableErrorCodeAndTransitionContext() {
        ObservationId id = ObservationId.random();

        ObservationTransitionError error =
                new ObservationTransitionError(
                        id,
                        ObservationStatus.ACCEPTED,
                        ObservationStatus.REJECTED
                );

        assertThat(error.code())
                .isEqualTo(
                        "observation.invalid-transition"
                );

        assertThat(error.observationId())
                .isEqualTo(id);

        assertThat(error.currentStatus())
                .isEqualTo(
                        ObservationStatus.ACCEPTED
                );

        assertThat(error.targetStatus())
                .isEqualTo(
                        ObservationStatus.REJECTED
                );

        assertThat(error.message())
                .contains(id.toString())
                .contains("ACCEPTED")
                .contains("REJECTED");
    }
}