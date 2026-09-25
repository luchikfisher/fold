package com.fold.kernel.events;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EventVersionTest {

    @Test
    void initialVersionShouldBeOne() {
        assertThat(EventVersion.initial().value())
                .isEqualTo(1);
    }

    @Test
    void shouldCreatePositiveVersion() {
        EventVersion version = EventVersion.of(3);

        assertThat(version.value()).isEqualTo(3);
    }

    @Test
    void shouldRejectZero() {
        assertThatThrownBy(() -> EventVersion.of(0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "event version must be greater than zero"
                );
    }

    @Test
    void shouldRejectNegativeVersion() {
        assertThatThrownBy(() -> EventVersion.of(-1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldCompareVersions() {
        assertThat(
                EventVersion.of(1)
                        .compareTo(EventVersion.of(2))
        ).isNegative();
    }
}