package com.fold.kernel.time;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TimestampTest {

    @ParameterizedTest
    @CsvSource({
            "2026-09-25T10:00:00.123456789Z, 2026-09-25T10:00:00.123456Z",
            "2026-09-25T10:00:00.000000999Z, 2026-09-25T10:00:00Z",
            "2026-09-25T10:00:00.999999999Z, 2026-09-25T10:00:00.999999Z",
            "1969-12-31T23:59:59.999999999Z, 1969-12-31T23:59:59.999999Z",
            "1969-12-31T23:59:59.123456789Z, 1969-12-31T23:59:59.123456Z",
            "2026-09-25T10:00:00.123456Z, 2026-09-25T10:00:00.123456Z",
            "2026-09-25T10:00:00Z, 2026-09-25T10:00:00Z",
            "2026-09-25T12:00:00.123456789+02:00, 2026-09-25T10:00:00.123456Z"
    })
    void allConstructionPathsShouldTruncateToMicroseconds(
            String input,
            String expected
    ) {
        Instant instant = Instant.parse(input);

        for (Timestamp timestamp : new Timestamp[]{
                new Timestamp(instant),
                Timestamp.of(instant),
                Timestamp.parse(input)
        }) {
            assertThat(timestamp.value()).isEqualTo(Instant.parse(expected));
            assertThat(timestamp.toString()).isEqualTo(expected);
            assertThat(Timestamp.of(timestamp.value())).isEqualTo(timestamp);
        }
    }

    @Test
    void subMicrosecondDifferencesShouldHaveEqualValuesHashesAndOrdering() {
        Timestamp first = Timestamp.parse("2026-09-25T10:00:00.123456100Z");
        Timestamp second = Timestamp.parse("2026-09-25T10:00:00.123456999Z");

        assertThat(first).isEqualTo(second).hasSameHashCodeAs(second);
        assertThat(first.compareTo(second)).isZero();
        assertThat(first.isBefore(second)).isFalse();
        assertThat(first.isAfter(second)).isFalse();
    }

    @Test
    void distinctMicrosecondsShouldRemainDistinctAndOrdered() {
        Timestamp first = Timestamp.parse("2026-09-25T10:00:00.123456999Z");
        Timestamp second = Timestamp.parse("2026-09-25T10:00:00.123457001Z");

        assertThat(first).isNotEqualTo(second);
        assertThat(first.compareTo(second)).isNegative();
        assertThat(first.isBefore(second)).isTrue();
        assertThat(second.isAfter(first)).isTrue();
    }

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
