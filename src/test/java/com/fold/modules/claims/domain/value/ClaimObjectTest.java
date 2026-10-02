package com.fold.modules.claims.domain.value;

import com.fold.kernel.time.Timestamp;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClaimObjectTest {

    @Test
    void referenceShouldWrapClaimReference() {
        ClaimReference reference =
                new ClaimReference(
                        "person-registry",
                        "42"
                );

        ClaimObject.Reference object =
                new ClaimObject.Reference(reference);

        assertThat(object.value())
                .isEqualTo(reference);
    }

    @Test
    void referenceShouldRejectNull() {
        assertThatThrownBy(
                () -> new ClaimObject.Reference(null)
        )
                .isInstanceOf(
                        NullPointerException.class
                );
    }

    @Test
    void textShouldPreserveExactValue() {
        ClaimObject.Text text =
                new ClaimObject.Text(" Alice ");

        assertThat(text.value())
                .isEqualTo(" Alice ");
    }

    @Test
    void emptyTextShouldRemainStructurallyValid() {
        ClaimObject.Text text =
                new ClaimObject.Text("");

        assertThat(text.value()).isEmpty();
    }

    @Test
    void textShouldRejectNull() {
        assertThatThrownBy(
                () -> new ClaimObject.Text(null)
        )
                .isInstanceOf(
                        NullPointerException.class
                );
    }

    @Test
    void equivalentDecimalScalesShouldCanonicalize() {
        ClaimObject.NumberValue first =
                new ClaimObject.NumberValue(
                        new BigDecimal("1.0")
                );

        ClaimObject.NumberValue second =
                new ClaimObject.NumberValue(
                        new BigDecimal("1.000")
                );

        assertThat(first).isEqualTo(second);
    }

    @Test
    void zeroShouldCanonicalizeRegardlessOfScale() {
        ClaimObject.NumberValue first =
                new ClaimObject.NumberValue(
                        new BigDecimal("0.000")
                );

        ClaimObject.NumberValue second =
                new ClaimObject.NumberValue(
                        BigDecimal.ZERO
                );

        assertThat(first).isEqualTo(second);
        assertThat(first.value())
                .isEqualTo(BigDecimal.ZERO);
    }

    @Test
    void numericFactoryShouldParseExactDecimal() {
        ClaimObject.NumberValue number =
                ClaimObject.NumberValue.of(
                        "123.4500"
                );

        assertThat(number.value())
                .isEqualTo(
                        new BigDecimal("123.45")
                );
    }

    @Test
    void differentNumbersShouldRemainDifferent() {
        assertThat(
                ClaimObject.NumberValue.of("1")
        )
                .isNotEqualTo(
                        ClaimObject.NumberValue.of("2")
                );
    }

    @Test
    void booleanShouldPreserveValue() {
        assertThat(
                new ClaimObject.BooleanValue(true)
                        .value()
        ).isTrue();
    }

    @Test
    void timestampShouldPreserveTimestamp() {
        Timestamp timestamp =
                Timestamp.parse(
                        "2026-10-02T10:00:00.123456789Z"
                );

        ClaimObject.TimestampValue value =
                new ClaimObject.TimestampValue(timestamp);

        assertThat(value.value())
                .isEqualTo(timestamp);
    }

    @Test
    void timestampShouldNotCanonicalizePrecision() {
        Timestamp timestamp =
                Timestamp.parse(
                        "2026-10-02T10:00:00.123456789Z"
                );

        ClaimObject.TimestampValue value =
                new ClaimObject.TimestampValue(timestamp);

        assertThat(value.value().value().getNano())
                .isEqualTo(123_456_789);
    }

    @Test
    void identifierShouldPreserveSchemeAndValue() {
        ClaimObject.Identifier identifier =
                new ClaimObject.Identifier(
                        "lei",
                        "549300ABC"
                );

        assertThat(identifier.scheme())
                .isEqualTo("lei");

        assertThat(identifier.value())
                .isEqualTo("549300ABC");
    }

    @Test
    void identifierShouldRejectBlankScheme() {
        assertThatThrownBy(
                () -> new ClaimObject.Identifier(
                        " ",
                        "123"
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                );
    }

    @Test
    void identifierShouldRejectBlankValue() {
        assertThatThrownBy(
                () -> new ClaimObject.Identifier(
                        "lei",
                        " "
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                );
    }

    @Test
    void textualOneAndNumericOneShouldRemainDifferentTypes() {
        ClaimObject text =
                new ClaimObject.Text("1");

        ClaimObject number =
                ClaimObject.NumberValue.of("1");

        assertThat(text)
                .isNotEqualTo(number);
    }

    @Test
    void referenceAndIdentifierShouldRemainDifferentTypes() {
        ClaimObject reference =
                new ClaimObject.Reference(
                        new ClaimReference(
                                "company-registry",
                                "123"
                        )
                );

        ClaimObject identifier =
                new ClaimObject.Identifier(
                        "company-registry",
                        "123"
                );

        assertThat(reference)
                .isNotEqualTo(identifier);
    }
}