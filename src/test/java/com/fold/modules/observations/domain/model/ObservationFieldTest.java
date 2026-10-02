package com.fold.modules.observations.domain.model;

import com.fold.modules.observations.domain.value.ObservationValue;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ObservationFieldTest {

    @Test
    void shouldAssociateSemanticNameWithTypedValue() {
        ObservationField field =
                ObservationField.of(
                        "organization.name",
                        ObservationValue.Text.of("Acme Ltd")
                );

        assertThat(field.name())
                .isEqualTo("organization.name");

        assertThat(field.value())
                .isEqualTo(
                        ObservationValue.Text.of("Acme Ltd")
                );
    }

    @Test
    void shouldRejectBlankFieldName() {
        assertThatThrownBy(
                () -> ObservationField.of(
                        " ",
                        ObservationValue.Text.of("value")
                )
        )
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectNullValue() {
        assertThatThrownBy(
                () -> ObservationField.of(
                        "organization.name",
                        null
                )
        )
                .isInstanceOf(NullPointerException.class);
    }
}