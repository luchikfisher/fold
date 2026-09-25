package com.fold.kernel.probability;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConfidenceTest {

    @Test
    void shouldCreateNormalizedConfidence() {
        Confidence confidence =
                Confidence.of(0.73);

        assertThat(confidence.decimal())
                .isEqualTo(0.73);

        assertThat(confidence.percentage())
                .isEqualTo(73.0);
    }

    @Test
    void noneShouldRepresentZeroConfidence() {
        assertThat(Confidence.NONE.decimal())
                .isZero();
    }

    @Test
    void certainShouldRepresentMaximumConfidence() {
        assertThat(Confidence.CERTAIN.decimal())
                .isEqualTo(1.0);
    }

    @Test
    void shouldRejectOutOfRangeConfidence() {
        assertThatThrownBy(
                () -> Confidence.of(1.1)
        )
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void confidenceShouldCompareByNormalizedValue() {
        assertThat(
                Confidence.of(0.3)
                        .compareTo(
                                Confidence.of(0.9)
                        )
        ).isNegative();
    }

    @Test
    void confidenceAndProbabilityRemainDifferentTypes() {
        Confidence confidence =
                Confidence.of(0.8);

        Probability probability =
                Probability.of(0.8);

        assertThat((Object) confidence)
                .isNotEqualTo(probability);
    }
}