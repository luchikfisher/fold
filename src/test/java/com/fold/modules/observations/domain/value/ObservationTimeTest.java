package com.fold.modules.observations.domain.value;

import com.fold.kernel.time.Timestamp;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ObservationTimeTest {

    private static final Timestamp T0 =
            Timestamp.parse("2026-01-01T00:00:00Z");

    private static final Timestamp T1 =
            Timestamp.parse("2026-01-02T00:00:00Z");

    @Test
    void observedAtShouldWrapSourceFacingTime() {
        ObservedAt observedAt =
                ObservedAt.of(T0);

        assertThat(observedAt.value())
                .isEqualTo(T0);
    }

    @Test
    void arrivedAtShouldWrapFoldArrivalTime() {
        ArrivedAt arrivedAt =
                ArrivedAt.of(T1);

        assertThat(arrivedAt.value())
                .isEqualTo(T1);
    }

    @Test
    void observedAtAndArrivedAtShouldRemainDifferentTypes() {
        ObservedAt observedAt =
                ObservedAt.of(T0);

        ArrivedAt arrivedAt =
                ArrivedAt.of(T0);

        assertThat((Object) observedAt)
                .isNotEqualTo(arrivedAt);
    }

    @Test
    void observedTimeMayBeAfterArrivalTime() {
        ObservedAt observedAt =
                ObservedAt.of(T1);

        ArrivedAt arrivedAt =
                ArrivedAt.of(T0);

        assertThat(observedAt.value().isAfter(arrivedAt.value()))
                .isTrue();
    }
}