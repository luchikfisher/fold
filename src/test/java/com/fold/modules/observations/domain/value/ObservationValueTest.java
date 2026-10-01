package com.fold.modules.observations.domain.value;

import com.fold.kernel.time.Timestamp;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ObservationValueTest {

    @Test
    void textShouldPreserveEmptyText() {
        ObservationValue.Text value =
                ObservationValue.Text.of("");

        assertThat(value.value()).isEmpty();
    }

    @Test
    void textShouldPreserveWhitespaceWhenItIsSemanticContent() {
        ObservationValue.Text value =
                ObservationValue.Text.of(" John Smith ");

        assertThat(value.value())
                .isEqualTo(" John Smith ");
    }

    @Test
    void numbersShouldNormalizeTrailingZeros() {
        ObservationValue.Number first =
                ObservationValue.Number.of(
                        new BigDecimal("1.0")
                );

        ObservationValue.Number second =
                ObservationValue.Number.of(
                        new BigDecimal("1.000")
                );

        assertThat(first)
                .isEqualTo(second);
    }

    @Test
    void zeroNumbersShouldNormalizeConsistently() {
        ObservationValue.Number first =
                ObservationValue.Number.of(
                        new BigDecimal("0.000")
                );

        ObservationValue.Number second =
                ObservationValue.Number.of(
                        BigDecimal.ZERO
                );

        assertThat(first)
                .isEqualTo(second);
    }

    @Test
    void doubleFactoryShouldRejectNaN() {
        assertThatThrownBy(
                () -> ObservationValue.Number.of(
                        Double.NaN
                )
        )
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void booleanShouldPreserveValue() {
        ObservationValue.BooleanValue value =
                ObservationValue.BooleanValue.of(true);

        assertThat(value.value()).isTrue();
    }

    @Test
    void timestampShouldPreserveTimestamp() {
        Timestamp timestamp =
                Timestamp.parse("2026-01-01T00:00:00Z");

        ObservationValue.TimestampValue value =
                ObservationValue.TimestampValue.of(
                        timestamp
                );

        assertThat(value.value())
                .isEqualTo(timestamp);
    }

    @Test
    void identifierShouldPreserveSchemeAndValue() {
        ObservationValue.Identifier identifier =
                ObservationValue.Identifier.of(
                        "lei",
                        "529900T8BM49AURSDO55"
                );

        assertThat(identifier.scheme())
                .isEqualTo("lei");

        assertThat(identifier.value())
                .isEqualTo("529900T8BM49AURSDO55");
    }

    @Test
    void identifierShouldRejectBlankScheme() {
        assertThatThrownBy(
                () -> ObservationValue.Identifier.of(
                        " ",
                        "123"
                )
        )
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void listShouldPreserveOrderAndRequireAtLeastOneValue() {
        ObservationValue.ListValue list =
                ObservationValue.ListValue.of(
                        ObservationValue.Text.of("first"),
                        ObservationValue.Text.of("second")
                );

        assertThat(list.asList())
                .containsExactly(
                        ObservationValue.Text.of("first"),
                        ObservationValue.Text.of("second")
                );
    }
}