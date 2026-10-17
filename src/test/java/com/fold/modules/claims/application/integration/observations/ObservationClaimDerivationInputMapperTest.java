package com.fold.modules.claims.application.integration.observations;

import com.fold.kernel.ids.EvidenceId;
import com.fold.kernel.ids.ObservationId;
import com.fold.kernel.ids.SourceId;
import com.fold.kernel.time.Timestamp;
import com.fold.modules.claims.domain.derivation.ClaimDerivationInput;
import com.fold.modules.claims.domain.derivation.ClaimDerivationValue;
import com.fold.modules.observations.api.view.ObservationFieldView;
import com.fold.modules.observations.api.view.ObservationValueView;
import com.fold.modules.observations.api.view.ObservationView;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ObservationClaimDerivationInputMapperTest {

    private final ObservationClaimDerivationInputMapper mapper =
            new ObservationClaimDerivationInputMapper();

    private static final ObservationId OBSERVATION_ID =
            ObservationId.random();

    private static final SourceId SOURCE_ID =
            SourceId.random();

    private static final EvidenceId EVIDENCE_ID =
            EvidenceId.random();

    private static final Timestamp OBSERVED_AT =
            Timestamp.parse(
                    "2026-10-03T10:00:00.123456789Z"
            );

    @Test
    void shouldMapObservationIdentityTypeAndSubject() {
        ClaimDerivationInput input =
                mapper.map(
                        observation(
                                List.of()
                        )
                );

        assertThat(input.observationId())
                .isEqualTo(OBSERVATION_ID);

        assertThat(input.type())
                .isEqualTo("test-type");

        assertThat(input.subject().type())
                .isEqualTo("organization");

        assertThat(input.subject().externalKey())
                .isEqualTo("registry:123");
    }

    @Test
    void shouldMapOriginReferences() {
        ClaimDerivationInput input =
                mapper.map(
                        observation(
                                List.of()
                        )
                );

        assertThat(input.origin().observationId())
                .isEqualTo(OBSERVATION_ID);

        assertThat(input.origin().sourceId())
                .isEqualTo(SOURCE_ID);

        assertThat(input.origin().evidence())
                .contains(EVIDENCE_ID);

        assertThat(input.origin().externalRecord())
                .contains("record-42");
    }

    @Test
    void shouldPreserveObservedAtExactly() {
        ClaimDerivationInput input =
                mapper.map(
                        observation(
                                List.of()
                        )
                );

        assertThat(input.observedAt())
                .isEqualTo(OBSERVED_AT);
    }

    @Test
    void shouldMapAllSupportedValueTypes() {
        ClaimDerivationInput input =
                mapper.map(
                        observation(
                                List.of(
                                        new ObservationFieldView(
                                                "text",
                                                new ObservationValueView.Text(
                                                        "hello"
                                                )
                                        ),
                                        new ObservationFieldView(
                                                "number",
                                                new ObservationValueView.Number(
                                                        new BigDecimal("1.2300")
                                                )
                                        ),
                                        new ObservationFieldView(
                                                "boolean",
                                                new ObservationValueView.BooleanValue(
                                                        true
                                                )
                                        ),
                                        new ObservationFieldView(
                                                "timestamp",
                                                new ObservationValueView.TimestampValue(
                                                        OBSERVED_AT
                                                )
                                        ),
                                        new ObservationFieldView(
                                                "identifier",
                                                new ObservationValueView.Identifier(
                                                        "lei",
                                                        "ABC123"
                                                )
                                        ),
                                        new ObservationFieldView(
                                                "list",
                                                new ObservationValueView.ListValue(
                                                        List.of(
                                                                new ObservationValueView.Text(
                                                                        "a"
                                                                ),
                                                                new ObservationValueView.Number(
                                                                        new BigDecimal("2")
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
                );

        assertThat(input.value("text"))
                .contains(
                        new ClaimDerivationValue.Text(
                                "hello"
                        )
                );

        assertThat(input.value("number"))
                .contains(
                        new ClaimDerivationValue.NumberValue(
                                new BigDecimal("1.2300")
                        )
                );

        assertThat(input.value("boolean"))
                .contains(
                        new ClaimDerivationValue.BooleanValue(
                                true
                        )
                );

        assertThat(input.value("timestamp"))
                .contains(
                        new ClaimDerivationValue.TimestampValue(
                                OBSERVED_AT
                        )
                );

        assertThat(input.value("identifier"))
                .contains(
                        new ClaimDerivationValue.Identifier(
                                "lei",
                                "ABC123"
                        )
                );

        assertThat(input.value("list"))
                .containsInstanceOf(
                        ClaimDerivationValue.ListValue.class
                );
    }

    private static ObservationView observation(
            List<ObservationFieldView> payload
    ) {
        return new ObservationView(
                OBSERVATION_ID,
                "test-type",
                new ObservationView.Subject(
                        "organization",
                        "registry:123"
                ),
                payload,
                new ObservationView.Origin(
                        SOURCE_ID,
                        EVIDENCE_ID,
                        "record-42"
                ),
                new ObservationView.Fingerprint(
                        1,
                        "SHA-256",
                        "a".repeat(64)
                ),
                OBSERVED_AT,
                Timestamp.parse(
                        "2026-10-03T10:01:00Z"
                ),
                "ACCEPTED",
                Timestamp.parse(
                        "2026-10-03T10:01:01Z"
                ),
                null
        );
    }
}