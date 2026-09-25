package com.fold.platform.time;

import com.fold.kernel.time.Timestamp;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SystemFoldClockTest {

    @Test
    void shouldReturnTimestampFromUnderlyingClock() {
        Instant instant =
                Instant.parse("2026-09-25T10:00:00Z");

        Clock fixedClock =
                Clock.fixed(
                        instant,
                        ZoneOffset.UTC
                );

        SystemFoldClock clock =
                new SystemFoldClock(fixedClock);

        assertThat(clock.now())
                .isEqualTo(Timestamp.of(instant));
    }

    @Test
    void shouldRejectNullClock() {
        assertThatThrownBy(
                () -> new SystemFoldClock(null)
        )
                .isInstanceOf(NullPointerException.class)
                .hasMessage("clock must not be null");
    }
}