package com.fold.modules.claims.domain.value;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClaimConfidenceTest {

    @Test
    void shouldCreateNormalizedClaimConfidence() {
        ClaimConfidence confidence =
                ClaimConfidence.of(0.82);

        assertThat(confidence.decimal())
                .isEqualTo(0.82);

        assertThat(confidence.percentage())
                .isEqualTo(82.0);
    }

    @Test
    void noneShouldRepresentZero() {
        assertThat(
                ClaimConfidence.NONE.decimal()
        ).isZero();
    }

    @Test
    void certainShouldRepresentOne() {
        assertThat(
                ClaimConfidence.CERTAIN.decimal()
        ).isEqualTo(1.0);
    }

    @Test
    void shouldRejectValueBelowZero() {
        assertThatThrownBy(
                () -> ClaimConfidence.of(-0.01)
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                );
    }

    @Test
    void shouldRejectValueAboveOne() {
        assertThatThrownBy(
                () -> ClaimConfidence.of(1.01)
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                );
    }

    @Test
    void shouldCompareClaimConfidence() {
        assertThat(
                ClaimConfidence.of(0.2)
                        .compareTo(
                                ClaimConfidence.of(0.8)
                        )
        ).isNegative();
    }
}