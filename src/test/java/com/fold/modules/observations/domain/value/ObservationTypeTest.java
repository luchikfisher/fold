package com.fold.modules.observations.domain.value;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ObservationTypeTest {

    @Test
    void shouldPreserveSemanticType() {
        ObservationType type =
                ObservationType.of("organization-registration");

        assertThat(type.value())
                .isEqualTo("organization-registration");

        assertThat(type.toString())
                .isEqualTo("organization-registration");
    }

    @Test
    void shouldRejectBlankType() {
        assertThatThrownBy(
                () -> ObservationType.of(" ")
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("value must not be blank");
    }

    @Test
    void shouldRejectSurroundingWhitespace() {
        assertThatThrownBy(
                () -> ObservationType.of(
                        " transaction "
                )
        )
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void equalTypesShouldBeEqual() {
        assertThat(
                ObservationType.of("transaction")
        ).isEqualTo(
                ObservationType.of("transaction")
        );
    }
}