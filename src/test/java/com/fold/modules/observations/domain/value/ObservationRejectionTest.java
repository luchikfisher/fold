package com.fold.modules.observations.domain.value;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ObservationRejectionTest {

    @Test
    void shouldRepresentStableReason() {
        ObservationRejection rejection =
                ObservationRejection.of(
                        "missing-subject",
                        "The observation does not identify a subject"
                );

        assertThat(rejection.code())
                .isEqualTo("missing-subject");

        assertThat(rejection.message())
                .isEqualTo(
                        "The observation does not identify a subject"
                );
    }

    @Test
    void shouldRejectBlankCode() {
        assertThatThrownBy(
                () -> ObservationRejection.of(
                        " ",
                        "Invalid observation"
                )
        )
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectBlankMessage() {
        assertThatThrownBy(
                () -> ObservationRejection.of(
                        "invalid",
                        " "
                )
        )
                .isInstanceOf(IllegalArgumentException.class);
    }
}