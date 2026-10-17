package com.fold.modules.claims.domain.value;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClaimRevisionTest {

    @Test
    void initialRevisionShouldBeOne() {
        assertThat(
                ClaimRevision.initial().value()
        ).isEqualTo(1);
    }

    @Test
    void nextShouldIncrementRevision() {
        ClaimRevision revision =
                new ClaimRevision(7);

        assertThat(revision.next())
                .isEqualTo(
                        new ClaimRevision(8)
                );
    }

    @Test
    void shouldRejectZeroRevision() {
        assertThatThrownBy(
                () -> new ClaimRevision(0)
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "value must be greater than zero"
                );
    }

    @Test
    void shouldRejectNegativeRevision() {
        assertThatThrownBy(
                () -> new ClaimRevision(-1)
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                );
    }

    @Test
    void shouldCompareRevisions() {
        assertThat(
                new ClaimRevision(2)
                        .compareTo(
                                new ClaimRevision(3)
                        )
        ).isNegative();
    }

    @Test
    void nextShouldRejectOverflow() {
        ClaimRevision revision =
                new ClaimRevision(Long.MAX_VALUE);

        assertThatThrownBy(revision::next)
                .isInstanceOf(
                        ArithmeticException.class
                );
    }
}