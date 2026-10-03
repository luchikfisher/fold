package com.fold.modules.claims.domain.derivation;

import com.fold.kernel.ids.ObservationId;
import com.fold.kernel.ids.SourceId;
import com.fold.kernel.time.Timestamp;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClaimDerivationInputTest {

    private static final Timestamp OBSERVED_AT =
            Timestamp.parse(
                    "2026-10-03T10:00:00Z"
            );

    @Test
    void shouldLookupFieldsByExactName() {
        ObservationId observationId =
                ObservationId.random();

        ClaimDerivationInput input =
                new ClaimDerivationInput(
                        observationId,
                        "company",
                        new ClaimDerivationSubject(
                                "organization",
                                "123"
                        ),
                        List.of(
                                new ClaimDerivationField(
                                        "country",
                                        new ClaimDerivationValue.Text(
                                                "Germany"
                                        )
                                )
                        ),
                        new ClaimDerivationOrigin(
                                observationId,
                                SourceId.random(),
                                null,
                                null
                        ),
                        OBSERVED_AT
                );

        assertThat(input.value("country"))
                .contains(
                        new ClaimDerivationValue.Text(
                                "Germany"
                        )
                );

        assertThat(input.value("Country"))
                .isEmpty();
    }

    @Test
    void shouldRejectDuplicateFieldNames() {
        ObservationId observationId =
                ObservationId.random();

        assertThatThrownBy(
                () -> new ClaimDerivationInput(
                        observationId,
                        "company",
                        new ClaimDerivationSubject(
                                "organization",
                                "123"
                        ),
                        List.of(
                                new ClaimDerivationField(
                                        "country",
                                        new ClaimDerivationValue.Text(
                                                "Germany"
                                        )
                                ),
                                new ClaimDerivationField(
                                        "country",
                                        new ClaimDerivationValue.Text(
                                                "France"
                                        )
                                )
                        ),
                        new ClaimDerivationOrigin(
                                observationId,
                                SourceId.random(),
                                null,
                                null
                        ),
                        OBSERVED_AT
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessageContaining(
                        "unique names"
                );
    }

    @Test
    void shouldRejectOriginForDifferentObservation() {
        assertThatThrownBy(
                () -> new ClaimDerivationInput(
                        ObservationId.random(),
                        "company",
                        new ClaimDerivationSubject(
                                "organization",
                                "123"
                        ),
                        List.of(),
                        new ClaimDerivationOrigin(
                                ObservationId.random(),
                                SourceId.random(),
                                null,
                                null
                        ),
                        OBSERVED_AT
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "origin ObservationId must match input ObservationId"
                );
    }
}