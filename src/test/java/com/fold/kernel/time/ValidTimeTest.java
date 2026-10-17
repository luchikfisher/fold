package com.fold.kernel.time;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ValidTimeTest {

    @Test
    void shouldRejectValidityWhoseBoundariesCollapseToSameMicrosecond() {
        assertThatThrownBy(() -> ValidTime.between(
                Timestamp.parse("2026-01-01T00:00:00.123456100Z"),
                Timestamp.parse("2026-01-01T00:00:00.123456999Z")
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldPreserveOneMicrosecondValidityAndExclusiveEnd() {
        Timestamp start = Timestamp.parse("2026-01-01T00:00:00.123456999Z");
        Timestamp end = Timestamp.parse("2026-01-01T00:00:00.123457001Z");
        ValidTime validTime = ValidTime.between(start, end);

        assertThat(validTime.contains(start)).isTrue();
        assertThat(validTime.contains(end)).isFalse();
        assertThat(validTime.overlaps(ValidTime.from(end))).isFalse();
    }

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
