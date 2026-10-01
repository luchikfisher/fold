package com.fold.kernel.time;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TimeRangeTest {

    private static final Timestamp T0 =
            Timestamp.parse("2026-09-25T10:00:00Z");

    private static final Timestamp T1 =
            Timestamp.parse("2026-09-25T11:00:00Z");

    private static final Timestamp T2 =
            Timestamp.parse("2026-09-25T12:00:00Z");

    private static final Timestamp T3 =
            Timestamp.parse("2026-09-25T13:00:00Z");

    @Test
    void finiteRangeShouldExposeBothBoundaries() {
        TimeRange range = TimeRange.between(T0, T2);

        assertThat(range.startInclusive()).isEqualTo(T0);
        assertThat(range.end()).contains(T2);
        assertThat(range.isOpenEnded()).isFalse();
    }

    @Test
    void openEndedRangeShouldHaveNoEnd() {
        TimeRange range = TimeRange.from(T0);

        assertThat(range.startInclusive()).isEqualTo(T0);
        assertThat(range.end()).isEmpty();
        assertThat(range.isOpenEnded()).isTrue();
    }

    @Test
    void rangeShouldIncludeItsStart() {
        TimeRange range = TimeRange.between(T0, T2);

        assertThat(range.contains(T0)).isTrue();
    }

    @Test
    void finiteRangeShouldExcludeItsEnd() {
        TimeRange range = TimeRange.between(T0, T2);

        assertThat(range.contains(T2)).isFalse();
    }

    @Test
    void rangeShouldContainInteriorTimestamp() {
        TimeRange range = TimeRange.between(T0, T2);

        assertThat(range.contains(T1)).isTrue();
    }

    @Test
    void rangeShouldRejectTimestampBeforeStart() {
        TimeRange range = TimeRange.between(T1, T3);

        assertThat(range.contains(T0)).isFalse();
    }

    @Test
    void openEndedRangeShouldContainAnyLaterTimestamp() {
        TimeRange range = TimeRange.from(T0);

        assertThat(range.contains(T3)).isTrue();
    }

    @Test
    void overlappingRangesShouldOverlapSymmetrically() {
        TimeRange first = TimeRange.between(T0, T2);
        TimeRange second = TimeRange.between(T1, T3);

        assertThat(first.overlaps(second)).isTrue();
        assertThat(second.overlaps(first)).isTrue();
    }

    @Test
    void adjacentRangesShouldNotOverlap() {
        TimeRange first = TimeRange.between(T0, T1);
        TimeRange second = TimeRange.between(T1, T2);

        assertThat(first.overlaps(second)).isFalse();
        assertThat(second.overlaps(first)).isFalse();
    }

    @Test
    void separatedRangesShouldNotOverlap() {
        TimeRange first = TimeRange.between(T0, T1);
        TimeRange second = TimeRange.between(T2, T3);

        assertThat(first.overlaps(second)).isFalse();
    }

    @Test
    void finiteRangeShouldOverlapOpenEndedRangeWhenTheyIntersect() {
        TimeRange finite = TimeRange.between(T1, T3);
        TimeRange open = TimeRange.from(T2);

        assertThat(finite.overlaps(open)).isTrue();
        assertThat(open.overlaps(finite)).isTrue();
    }

    @Test
    void openEndedRangesShouldOverlapWhenSecondStartsLater() {
        TimeRange first = TimeRange.from(T0);
        TimeRange second = TimeRange.from(T2);

        assertThat(first.overlaps(second)).isTrue();
    }

    @Test
    void shouldRejectZeroLengthRange() {
        assertThatThrownBy(
                () -> TimeRange.between(T1, T1)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "startInclusive must be before endExclusive"
                );
    }

    @Test
    void shouldRejectInvertedRange() {
        assertThatThrownBy(
                () -> TimeRange.between(T2, T1)
        )
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectNullStart() {
        assertThatThrownBy(
                () -> TimeRange.from(null)
        )
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void containsShouldRejectNullTimestamp() {
        TimeRange range = TimeRange.from(T0);

        assertThatThrownBy(() -> range.contains(null))
                .isInstanceOf(NullPointerException.class);
    }
}