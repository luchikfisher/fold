package com.fold.modules.claims.domain.value;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClaimDerivationTest {

    @Test
    void methodShouldPreserveNameAndVersion() {
        ClaimDerivationMethod method =
                new ClaimDerivationMethod(
                        "company-director-extractor",
                        2
                );

        assertThat(method.name())
                .isEqualTo(
                        "company-director-extractor"
                );

        assertThat(method.version())
                .isEqualTo(2);
    }

    @Test
    void versionOneFactoryShouldCreateInitialVersion() {
        ClaimDerivationMethod method =
                ClaimDerivationMethod.versionOne(
                        "company-director-extractor"
                );

        assertThat(method.version())
                .isEqualTo(1);
    }

    @Test
    void methodShouldRejectBlankName() {
        assertThatThrownBy(
                () -> new ClaimDerivationMethod(
                        " ",
                        1
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "name must not be blank"
                );
    }

    @Test
    void methodShouldRejectNonPositiveVersion() {
        assertThatThrownBy(
                () -> new ClaimDerivationMethod(
                        "extractor",
                        0
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "version must be greater than zero"
                );
    }

    @Test
    void derivationShouldCombineKindAndMethod() {
        ClaimDerivationMethod method =
                ClaimDerivationMethod.versionOne(
                        "employment-extractor"
                );

        ClaimDerivation derivation =
                new ClaimDerivation(
                        ClaimDerivationKind.OBSERVED,
                        method
                );

        assertThat(derivation.kind())
                .isEqualTo(
                        ClaimDerivationKind.OBSERVED
                );

        assertThat(derivation.method())
                .isEqualTo(method);
    }

    @Test
    void derivationKindsShouldRemainDistinct() {
        assertThat(ClaimDerivationKind.values())
                .containsExactly(
                        ClaimDerivationKind.OBSERVED,
                        ClaimDerivationKind.DERIVED,
                        ClaimDerivationKind.INFERRED,
                        ClaimDerivationKind.ANALYST_ENTERED
                );
    }

    @Test
    void derivationShouldRejectNullKind() {
        assertThatThrownBy(
                () -> new ClaimDerivation(
                        null,
                        ClaimDerivationMethod
                                .versionOne("test")
                )
        )
                .isInstanceOf(
                        NullPointerException.class
                );
    }

    @Test
    void derivationShouldRejectNullMethod() {
        assertThatThrownBy(
                () -> new ClaimDerivation(
                        ClaimDerivationKind.OBSERVED,
                        null
                )
        )
                .isInstanceOf(
                        NullPointerException.class
                );
    }
}