package com.fold.modules.claims.infrastructure.persistence.mapper;

import com.fold.kernel.time.Timestamp;
import com.fold.modules.claims.domain.value.ClaimObject;
import com.fold.modules.claims.domain.value.ClaimReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClaimObjectJsonCodecTest {

    private ClaimObjectJsonCodec codec;

    @BeforeEach
    void setUp() {
        codec =
                new ClaimObjectJsonCodec(
                        new ObjectMapper()
                );
    }

    @Test
    void shouldRoundTripReference() {
        roundTrip(
                new ClaimObject.Reference(
                        new ClaimReference(
                                "person-registry",
                                "123"
                        )
                )
        );
    }

    @Test
    void shouldRoundTripText() {
        roundTrip(
                new ClaimObject.Text(
                        "hello"
                )
        );
    }

    @Test
    void shouldRoundTripNumber() {
        roundTrip(
                new ClaimObject.NumberValue(
                        new BigDecimal(
                                "123.4500"
                        )
                )
        );
    }

    @Test
    void shouldRoundTripBoolean() {
        roundTrip(
                new ClaimObject.BooleanValue(
                        true
                )
        );
    }

    @Test
    void shouldRoundTripCanonicalTimestampExactly() {
        ClaimObject.TimestampValue value =
                new ClaimObject.TimestampValue(
                        Timestamp.parse(
                                "2026-10-03T10:00:00.123456789Z"
                        )
                );

        ClaimObject decoded =
                codec.decode(
                        codec.encode(value)
                );

        assertThat(decoded)
                .isEqualTo(value);
        assertThat(codec.encode(value))
                .contains("2026-10-03T10:00:00.123456Z");
    }

    @Test
    void shouldDecodeFinerPrecisionThroughTimestampInvariant() {
        ClaimObject decoded = codec.decode("""
                {"type":"timestamp","value":"2026-10-03T10:00:00.123456789Z"}
                """);

        assertThat(decoded).isEqualTo(new ClaimObject.TimestampValue(
                Timestamp.parse("2026-10-03T10:00:00.123456Z")
        ));
    }

    @Test
    void shouldRoundTripIdentifier() {
        roundTrip(
                new ClaimObject.Identifier(
                        "lei",
                        "549300ABC"
                )
        );
    }

    @Test
    void malformedJsonShouldBeRejected() {
        assertThatThrownBy(
                () -> codec.decode(
                        "{not-json"
                )
        )
                .isInstanceOf(
                        IllegalStateException.class
                );
    }

    @Test
    void unknownTypeShouldBeRejected() {
        assertThatThrownBy(
                () -> codec.decode(
                        """
                        {
                          "type": "unknown",
                          "value": "x"
                        }
                        """
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessageContaining(
                        "Unsupported persisted ClaimObject type"
                );
    }

    @Test
    void missingRequiredFieldShouldBeRejected() {
        assertThatThrownBy(
                () -> codec.decode(
                        """
                        {
                          "type": "identifier",
                          "scheme": "lei"
                        }
                        """
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                );
    }

    private void roundTrip(
            ClaimObject object
    ) {
        String json =
                codec.encode(object);

        ClaimObject decoded =
                codec.decode(json);

        assertThat(decoded)
                .isEqualTo(object);
    }
}
