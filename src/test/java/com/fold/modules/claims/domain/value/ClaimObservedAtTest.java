package com.fold.modules.claims.domain.value;

import com.fold.kernel.time.Timestamp;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClaimObservedAtTest {

    @Test
    void shouldPreserveCanonicalTimestampExactly() {
        Timestamp timestamp =
                Timestamp.parse(
                        "2026-10-02T10:00:00.123456789Z"
                );

        ClaimObservedAt observedAt =
                new ClaimObservedAt(timestamp);

        assertThat(observedAt.value())
                .isEqualTo(timestamp);

        assertThat(
                observedAt.value()
                        .value()
                        .getNano()
        ).isEqualTo(123_456_000);
    }

    @Test
    void subMicrosecondDifferencesShouldBeEqual() {
        ClaimObservedAt first =
                ClaimObservedAt.of(
                        Timestamp.parse(
                                "2026-10-02T10:00:00.123456100Z"
                        )
                );

        ClaimObservedAt second =
                ClaimObservedAt.of(
                        Timestamp.parse(
                                "2026-10-02T10:00:00.123456900Z"
                        )
                );

        assertThat(first)
                .isEqualTo(second);
    }

    @Test
    void shouldRejectNullTimestamp() {
        assertThatThrownBy(
                () -> new ClaimObservedAt(null)
        )
                .isInstanceOf(
                        NullPointerException.class
                )
                .hasMessage(
                        "value must not be null"
                );
    }
}