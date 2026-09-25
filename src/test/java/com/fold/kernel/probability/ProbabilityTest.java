package com.fold.kernel.probability;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProbabilityTest {

    @Test
    void zeroShouldBeValid() {
        assertThat(Probability.ZERO.value())
                .isZero();
    }

    @Test
    void oneShouldBeValid() {
        assertThat(Probability.ONE.value())
                .isEqualTo(1.0);
    }

    @Test
    void valueBetweenZeroAndOneShouldBeValid() {
        Probability probability =
                Probability.of(0.42);

        assertThat(probability.value())
                .isEqualTo(0.42);
    }

    @Test
    void shouldConvertToPercentage() {
        assertThat(
                Probability.of(0.875).percentage()
        )
                .isEqualTo(87.5);
    }

    @Test
    void shouldRejectNegativeProbability() {
        assertThatThrownBy(
                () -> Probability.of(-0.01)
        )
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectProbabilityAboveOne() {
        assertThatThrownBy(
                () -> Probability.of(1.01)
        )
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectNaN() {
        assertThatThrownBy(
                () -> Probability.of(Double.NaN)
        )
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectPositiveInfinity() {
        assertThatThrownBy(
                () -> Probability.of(
                        Double.POSITIVE_INFINITY
                )
        )
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldCompareProbabilities() {
        assertThat(
                Probability.of(0.2)
                        .compareTo(
                                Probability.of(0.8)
                        )
        ).isNegative();
    }
}