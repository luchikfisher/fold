package com.fold.modules.claims.domain.value;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClaimPredicateTest {

    @Test
    void shouldPreservePredicateExactly() {
        ClaimPredicate predicate =
                ClaimPredicate.of("hasDirector");

        assertThat(predicate.value())
                .isEqualTo("hasDirector");

        assertThat(predicate.toString())
                .isEqualTo("hasDirector");
    }

    @Test
    void shouldNotSilentlyNormalizePredicate() {
        ClaimPredicate predicate =
                ClaimPredicate.of(" Has-Director ");

        assertThat(predicate.value())
                .isEqualTo(" Has-Director ");
    }

    @Test
    void shouldRejectNullPredicate() {
        assertThatThrownBy(
                () -> ClaimPredicate.of(null)
        )
                .isInstanceOf(
                        NullPointerException.class
                )
                .hasMessage(
                        "value must not be null"
                );
    }

    @Test
    void shouldRejectBlankPredicate() {
        assertThatThrownBy(
                () -> ClaimPredicate.of(" ")
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "value must not be blank"
                );
    }
}