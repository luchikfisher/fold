package com.fold.modules.observations.domain.model;

import com.fold.modules.observations.domain.value.ObservationValue;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ObservationPayloadTest {

    private static final ObservationField NAME =
            ObservationField.of(
                    "organization.name",
                    ObservationValue.Text.of("Acme Ltd")
            );

    private static final ObservationField COUNTRY =
            ObservationField.of(
                    "organization.country",
                    ObservationValue.Text.of("GB")
            );

    @Test
    void payloadShouldContainAtLeastOneField() {
        ObservationPayload payload =
                ObservationPayload.of(NAME);

        assertThat(payload.size()).isEqualTo(1);
    }

    @Test
    void payloadShouldPreserveFieldOrder() {
        ObservationPayload payload =
                ObservationPayload.of(
                        NAME,
                        COUNTRY
                );

        assertThat(payload.asList())
                .containsExactly(
                        NAME,
                        COUNTRY
                );
    }

    @Test
    void shouldFindFieldByExactName() {
        ObservationPayload payload =
                ObservationPayload.of(
                        NAME,
                        COUNTRY
                );

        assertThat(
                payload.find("organization.country")
        )
                .contains(COUNTRY);
    }

    @Test
    void shouldReportMissingField() {
        ObservationPayload payload =
                ObservationPayload.of(NAME);

        assertThat(
                payload.find("organization.registration-number")
        ).isEmpty();
    }

    @Test
    void shouldRejectDuplicateFieldNames() {
        ObservationField first =
                ObservationField.of(
                        "organization.name",
                        ObservationValue.Text.of("Acme")
                );

        ObservationField second =
                ObservationField.of(
                        "organization.name",
                        ObservationValue.Text.of("Acme Ltd")
                );

        assertThatThrownBy(
                () -> ObservationPayload.of(
                        first,
                        second
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(
                        "organization.name"
                );
    }

    @Test
    void multiValuedFieldShouldUseListValue() {
        ObservationField aliases =
                ObservationField.of(
                        "organization.aliases",
                        ObservationValue.ListValue.of(
                                ObservationValue.Text.of("Acme"),
                                ObservationValue.Text.of("Acme Ltd")
                        )
                );

        ObservationPayload payload =
                ObservationPayload.of(aliases);

        assertThat(payload.size()).isEqualTo(1);
    }
}