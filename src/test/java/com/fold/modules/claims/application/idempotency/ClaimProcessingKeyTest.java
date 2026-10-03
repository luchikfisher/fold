package com.fold.modules.claims.application.idempotency;

import com.fold.kernel.ids.ObservationId;
import com.fold.modules.claims.domain.value.ClaimDerivation;
import com.fold.modules.claims.domain.value.ClaimDerivationKind;
import com.fold.modules.claims.domain.value.ClaimDerivationMethod;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClaimProcessingKeyTest {

    private static ClaimDerivation derivation(
            ClaimDerivationKind kind,
            String method,
            int version
    ) {
        return new ClaimDerivation(
                kind,
                new ClaimDerivationMethod(
                        method,
                        version
                )
        );
    }

    @Test
    void sameObservationAndDerivationShouldProduceSameKey() {
        ObservationId observationId =
                ObservationId.random();

        ClaimDerivation derivation =
                derivation(
                        ClaimDerivationKind.OBSERVED,
                        "company-director-extractor",
                        1
                );

        ClaimProcessingKey first =
                new ClaimProcessingKey(
                        observationId,
                        derivation
                );

        ClaimProcessingKey second =
                new ClaimProcessingKey(
                        observationId,
                        derivation
                );

        assertThat(first).isEqualTo(second);
    }

    @Test
    void differentObservationShouldProduceDifferentKey() {
        ClaimDerivation derivation =
                derivation(
                        ClaimDerivationKind.OBSERVED,
                        "company-director-extractor",
                        1
                );

        ClaimProcessingKey first =
                new ClaimProcessingKey(
                        ObservationId.random(),
                        derivation
                );

        ClaimProcessingKey second =
                new ClaimProcessingKey(
                        ObservationId.random(),
                        derivation
                );

        assertThat(first)
                .isNotEqualTo(second);
    }

    @Test
    void differentMethodShouldProduceDifferentKey() {
        ObservationId observationId =
                ObservationId.random();

        ClaimProcessingKey first =
                new ClaimProcessingKey(
                        observationId,
                        derivation(
                                ClaimDerivationKind.OBSERVED,
                                "director-extractor",
                                1
                        )
                );

        ClaimProcessingKey second =
                new ClaimProcessingKey(
                        observationId,
                        derivation(
                                ClaimDerivationKind.OBSERVED,
                                "ownership-extractor",
                                1
                        )
                );

        assertThat(first)
                .isNotEqualTo(second);
    }

    @Test
    void differentMethodVersionShouldProduceDifferentKey() {
        ObservationId observationId =
                ObservationId.random();

        ClaimProcessingKey versionOne =
                new ClaimProcessingKey(
                        observationId,
                        derivation(
                                ClaimDerivationKind.OBSERVED,
                                "director-extractor",
                                1
                        )
                );

        ClaimProcessingKey versionTwo =
                new ClaimProcessingKey(
                        observationId,
                        derivation(
                                ClaimDerivationKind.OBSERVED,
                                "director-extractor",
                                2
                        )
                );

        assertThat(versionOne)
                .isNotEqualTo(versionTwo);
    }

    @Test
    void differentDerivationKindShouldProduceDifferentKey() {
        ObservationId observationId =
                ObservationId.random();

        ClaimProcessingKey observed =
                new ClaimProcessingKey(
                        observationId,
                        derivation(
                                ClaimDerivationKind.OBSERVED,
                                "relationship-rule",
                                1
                        )
                );

        ClaimProcessingKey inferred =
                new ClaimProcessingKey(
                        observationId,
                        derivation(
                                ClaimDerivationKind.INFERRED,
                                "relationship-rule",
                                1
                        )
                );

        assertThat(observed)
                .isNotEqualTo(inferred);
    }

    @Test
    void shouldRejectNullObservationId() {
        assertThatThrownBy(
                () -> new ClaimProcessingKey(
                        null,
                        derivation(
                                ClaimDerivationKind.OBSERVED,
                                "test",
                                1
                        )
                )
        )
                .isInstanceOf(
                        NullPointerException.class
                )
                .hasMessage(
                        "observationId must not be null"
                );
    }

    @Test
    void shouldRejectNullDerivation() {
        assertThatThrownBy(
                () -> new ClaimProcessingKey(
                        ObservationId.random(),
                        null
                )
        )
                .isInstanceOf(
                        NullPointerException.class
                )
                .hasMessage(
                        "derivation must not be null"
                );
    }
}