package com.fold.kernel.time;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ValidTimeTest {

    private static final Timestamp T0 =
            Timestamp.parse("2026-01-01T00:00:00Z");

    private static final Timestamp T1 =
            Timestamp.parse("2026-02-01T00:00:00Z");

    private static final Timestamp T2 =
            Timestamp.parse("2026-03-01T00:00:00Z");

    @Test
    void shouldRepresentFiniteValidityPeriod() {
        ValidTime validTime =
                ValidTime.between(T0, T2);

        assertThat(validTime.contains(T0)).isTrue();
        assertThat(validTime.contains(T1)).isTrue();
        assertThat(validTime.contains(T2)).isFalse();
    }

    @Test
    void shouldRepresentOpenEndedValidityPeriod() {
        ValidTime validTime =
                ValidTime.from(T0);

        assertThat(validTime.contains(T2)).isTrue();
    }

    @Test
    void shouldDetectTemporalOverlap() {
        ValidTime first =
                ValidTime.between(T0, T2);

        ValidTime second =
                ValidTime.from(T1);

        assertThat(first.overlaps(second)).isTrue();
    }
}