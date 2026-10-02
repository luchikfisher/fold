package com.fold.modules.observations.domain.value;

import com.fold.kernel.collections.NonEmptyList;
import com.fold.kernel.time.Timestamp;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * Represents a typed semantic value contained in an observation payload.
 *
 * <p>Observation values are deliberately independent of JSON, database types,
 * and source-specific serialization. They describe the normalized semantic
 * value received by FOLD.</p>
 *
 * <p>The observations domain supports scalar values and ordered lists of
 * values. Arbitrary nested objects are intentionally excluded. Complex raw
 * source structures belong to evidence or upstream normalization rather than
 * to the semantic observation model.</p>
 */
public sealed interface ObservationValue
        permits ObservationValue.Text,
        ObservationValue.Number,
        ObservationValue.BooleanValue,
        ObservationValue.TimestampValue,
        ObservationValue.Identifier,
        ObservationValue.ListValue {

    /**
     * Represents textual semantic content.
     *
     * @param value the text
     */
    record Text(String value) implements ObservationValue {

        public Text {
            Objects.requireNonNull(value, "value must not be null");
        }

        public static Text of(String value) {
            return new Text(value);
        }
    }

    /**
     * Represents a finite decimal number.
     *
     * <p>The value is normalized so that numerically equal decimal values have
     * the same representation. For example, {@code 1.0} and {@code 1.00}
     * normalize to the same value.</p>
     *
     * @param value the normalized decimal value
     */
    record Number(BigDecimal value)
            implements ObservationValue {

        public Number {
            Objects.requireNonNull(value, "value must not be null");

            value = normalize(value);
        }

        public static Number of(BigDecimal value) {
            return new Number(value);
        }

        public static Number of(long value) {
            return new Number(BigDecimal.valueOf(value));
        }

        public static Number of(double value) {
            if (!Double.isFinite(value)) {
                throw new IllegalArgumentException(
                        "value must be finite"
                );
            }

            return new Number(BigDecimal.valueOf(value));
        }

        private static BigDecimal normalize(BigDecimal value) {
            if (value.signum() == 0) {
                return BigDecimal.ZERO;
            }

            return value.stripTrailingZeros();
        }
    }

    /**
     * Represents a boolean semantic value.
     *
     * @param value the boolean value
     */
    record BooleanValue(boolean value)
            implements ObservationValue {

        public static BooleanValue of(boolean value) {
            return new BooleanValue(value);
        }
    }

    /**
     * Represents an absolute timestamp supplied as semantic data.
     *
     * @param value the timestamp
     */
    record TimestampValue(Timestamp value)
            implements ObservationValue {

        public TimestampValue {
            Objects.requireNonNull(value, "value must not be null");
        }

        public static TimestampValue of(Timestamp value) {
            return new TimestampValue(value);
        }
    }

    /**
     * Represents an identifier expressed within a named identifier scheme.
     *
     * <p>The scheme defines the namespace in which the value is meaningful.
     * Examples include {@code lei}, {@code company-registry}, or
     * {@code source-customer-id}.</p>
     *
     * @param scheme the identifier namespace or scheme
     * @param value  the identifier within that scheme
     */
    record Identifier(
            String scheme,
            String value
    ) implements ObservationValue {

        public Identifier {
            Objects.requireNonNull(scheme, "scheme must not be null");
            Objects.requireNonNull(value, "value must not be null");

            if (scheme.isBlank()) {
                throw new IllegalArgumentException(
                        "scheme must not be blank"
                );
            }

            if (value.isBlank()) {
                throw new IllegalArgumentException(
                        "value must not be blank"
                );
            }

            if (!scheme.equals(scheme.trim())) {
                throw new IllegalArgumentException(
                        "scheme must not contain leading or trailing whitespace"
                );
            }

            if (!value.equals(value.trim())) {
                throw new IllegalArgumentException(
                        "value must not contain leading or trailing whitespace"
                );
            }
        }

        public static Identifier of(
                String scheme,
                String value
        ) {
            return new Identifier(scheme, value);
        }
    }

    /**
     * Represents an ordered, non-empty collection of semantic values.
     *
     * <p>A list is used when one semantic field legitimately has multiple
     * values. This avoids representing repeated field names in an observation
     * payload.</p>
     *
     * @param values the ordered values
     */
    record ListValue(
            NonEmptyList<ObservationValue> values
    ) implements ObservationValue {

        public ListValue {
            Objects.requireNonNull(values, "values must not be null");
        }

        public static ListValue of(
                ObservationValue first,
                ObservationValue... remaining
        ) {
            return new ListValue(
                    NonEmptyList.of(first, remaining)
            );
        }

        public List<ObservationValue> asList() {
            return values.asList();
        }
    }
}