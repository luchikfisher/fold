package com.fold.kernel.time;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TimestampTest {

    @Test
    void shouldWrapInstant() {
        Instant instant = Instant.parse("2026-09-25T10:00:00Z");

        Timestamp timestamp = Timestamp.of(instant);

        assertThat(timestamp.value()).isEqualTo(instant);
    }

    @Test
    void shouldParseIsoInstant() {
        Timestamp timestamp =
                Timestamp.parse("2026-09-25T10:00:00Z");

        assertThat(timestamp.value())
                .isEqualTo(
                        Instant.parse("2026-09-25T10:00:00Z")
                );
    }

    @Test
    void shouldRejectNullInstant() {
        assertThatThrownBy(() -> new Timestamp(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("value must not be null");
    }

    @Test
    void shouldRejectBlankText() {
        assertThatThrownBy(() -> Timestamp.parse(" "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("value must not be blank");
    }

    @Test
    void shouldCompareChronologically() {
        Timestamp earlier =
                Timestamp.parse("2026-09-25T10:00:00Z");

        Timestamp later =
                Timestamp.parse("2026-09-25T11:00:00Z");

        assertThat(earlier.isBefore(later)).isTrue();
        assertThat(later.isAfter(earlier)).isTrue();
        assertThat(earlier.compareTo(later)).isNegative();
    }

    @Test
    void equalTimestampsShouldCompareAsEqual() {
        Timestamp first =
                Timestamp.parse("2026-09-25T10:00:00Z");

        Timestamp second =
                Timestamp.parse("2026-09-25T10:00:00Z");

        assertThat(first.compareTo(second)).isZero();
        assertThat(first).isEqualTo(second);
    }

    @Test
    void toStringShouldUseInstantRepresentation() {
        Timestamp timestamp =
                Timestamp.parse("2026-09-25T10:00:00Z");

        assertThat(timestamp.toString())
                .isEqualTo("2026-09-25T10:00:00Z");
    }
}