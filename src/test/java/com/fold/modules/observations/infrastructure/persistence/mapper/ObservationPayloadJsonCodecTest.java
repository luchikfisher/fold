package com.fold.modules.observations.infrastructure.persistence.mapper;

import com.fold.kernel.time.Timestamp;
import com.fold.modules.observations.domain.model.ObservationField;
import com.fold.modules.observations.domain.model.ObservationPayload;
import com.fold.modules.observations.domain.value.ObservationValue;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ObservationPayloadJsonCodecTest {

    private final ObservationPayloadJsonCodec codec =
            new ObservationPayloadJsonCodec(
                    new ObjectMapper()
            );

    @Test
    void shouldRoundTripEverySupportedValueType() {
        ObservationPayload original =
                ObservationPayload.of(
                        ObservationField.of(
                                "text",
                                ObservationValue.Text.of("")
                        ),
                        ObservationField.of(
                                "number",
                                ObservationValue.Number.of(
                                        new BigDecimal("123.4500")
                                )
                        ),
                        ObservationField.of(
                                "boolean",
                                ObservationValue.BooleanValue.of(true)
                        ),
                        ObservationField.of(
                                "timestamp",
                                ObservationValue.TimestampValue.of(
                                        Timestamp.parse(
                                                "2026-10-01T12:30:00Z"
                                        )
                                )
                        ),
                        ObservationField.of(
                                "identifier",
                                ObservationValue.Identifier.of(
                                        "lei",
                                        "529900T8BM49AURSDO55"
                                )
                        ),
                        ObservationField.of(
                                "list",
                                ObservationValue.ListValue.of(
                                        ObservationValue.Text.of("first"),
                                        ObservationValue.Number.of(2),
                                        ObservationValue.ListValue.of(
                                                ObservationValue.BooleanValue.of(false)
                                        )
                                )
                        )
                );

        String encoded =
                codec.encode(original);

        ObservationPayload restored =
                codec.decode(encoded);

        assertThat(restored)
                .isEqualTo(original);
    }

    @Test
    void shouldPreservePayloadFieldOrder() {
        ObservationPayload original =
                ObservationPayload.of(
                        ObservationField.of(
                                "third",
                                ObservationValue.Text.of("3")
                        ),
                        ObservationField.of(
                                "first",
                                ObservationValue.Text.of("1")
                        ),
                        ObservationField.of(
                                "second",
                                ObservationValue.Text.of("2")
                        )
                );

        ObservationPayload restored =
                codec.decode(
                        codec.encode(original)
                );

        assertThat(
                restored.asList()
                        .stream()
                        .map(ObservationField::name)
        ).containsExactly(
                "third",
                "first",
                "second"
        );
    }

    @Test
    void shouldRejectNonArrayRoot() {
        assertThatThrownBy(
                () -> codec.decode(
                        """
                                {
                                  "name": "value"
                                }
                                """
                )
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(
                        "must be a JSON array"
                );
    }

    @Test
    void shouldRejectEmptyPersistedPayload() {
        assertThatThrownBy(
                () -> codec.decode("[]")
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(
                        "must not be empty"
                );
    }

    @Test
    void shouldRejectUnknownValueType() {
        assertThatThrownBy(
                () -> codec.decode(
                        """
                                [
                                  {
                                    "name": "field",
                                    "value": {
                                      "type": "something-new",
                                      "value": "x"
                                    }
                                  }
                                ]
                                """
                )
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(
                        "Unknown persisted observation value type"
                );
    }

    @Test
    void shouldRejectEmptyPersistedListValue() {
        assertThatThrownBy(
                () -> codec.decode(
                        """
                                [
                                  {
                                    "name": "field",
                                    "value": {
                                      "type": "list",
                                      "values": []
                                    }
                                  }
                                ]
                                """
                )
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(
                        "must be a non-empty array"
                );
    }

    @Test
    void shouldRejectNumberStoredAsString() {
        assertThatThrownBy(
                () -> codec.decode(
                        """
                                [
                                  {
                                    "name": "field",
                                    "value": {
                                      "type": "number",
                                      "value": "12.5"
                                    }
                                  }
                                ]
                                """
                )
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(
                        "must be numeric"
                );
    }

    @Test
    void shouldRejectBooleanStoredAsString() {
        assertThatThrownBy(
                () -> codec.decode(
                        """
                                [
                                  {
                                    "name": "field",
                                    "value": {
                                      "type": "boolean",
                                      "value": "true"
                                    }
                                  }
                                ]
                                """
                )
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(
                        "must be boolean"
                );
    }
}