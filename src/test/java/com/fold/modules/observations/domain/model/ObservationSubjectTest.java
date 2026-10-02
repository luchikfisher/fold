package com.fold.modules.observations.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ObservationSubjectTest {

    @Test
    void shouldRepresentSourceFacingSubject() {
        ObservationSubject subject =
                ObservationSubject.of(
                        "organization",
                        "uk-companies-house:01234567"
                );

        assertThat(subject.type())
                .isEqualTo("organization");

        assertThat(subject.externalKey())
                .isEqualTo(
                        "uk-companies-house:01234567"
                );
    }

    @Test
    void shouldRejectBlankType() {
        assertThatThrownBy(
                () -> ObservationSubject.of(
                        " ",
                        "123"
                )
        )
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectBlankExternalKey() {
        assertThatThrownBy(
                () -> ObservationSubject.of(
                        "organization",
                        " "
                )
        )
                .isInstanceOf(IllegalArgumentException.class);
    }
}