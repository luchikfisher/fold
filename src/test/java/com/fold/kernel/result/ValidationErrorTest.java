package com.fold.kernel.result;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ValidationErrorTest {

    @Test
    void shouldExposeCodeAndMessage() {
        ValidationError error =
                new ValidationError(
                        "invalid-name",
                        "Name must not be blank"
                );

        assertThat(error.code())
                .isEqualTo("invalid-name");

        assertThat(error.message())
                .isEqualTo("Name must not be blank");
    }

    @Test
    void shouldRejectBlankCode() {
        assertThatThrownBy(
                () -> new ValidationError(
                        " ",
                        "Invalid"
                )
        )
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectBlankMessage() {
        assertThatThrownBy(
                () -> new ValidationError(
                        "invalid",
                        " "
                )
        )
                .isInstanceOf(IllegalArgumentException.class);
    }
}