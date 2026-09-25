package com.fold.kernel.probability;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ScoreTest {

    @Test
    void scoreMayBeGreaterThanOne() {
        Score score = Score.of(42.5);

        assertThat(score.value())
                .isEqualTo(42.5);
    }

    @Test
    void scoreMayBeNegative() {
        Score score = Score.of(-3.5);

        assertThat(score.value())
                .isEqualTo(-3.5);
    }

    @Test
    void shouldRejectNaN() {
        assertThatThrownBy(
                () -> Score.of(Double.NaN)
        )
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectInfinity() {
        assertThatThrownBy(
                () -> Score.of(
                        Double.NEGATIVE_INFINITY
                )
        )
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldCompareScores() {
        assertThat(
                Score.of(1)
                        .compareTo(Score.of(2))
        ).isNegative();
    }
}