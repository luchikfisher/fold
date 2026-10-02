package com.fold.modules.claims.domain.value;

import com.fold.kernel.time.Timestamp;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Represents the object or value asserted by a claim.
 *
 * <p>A claim object is strongly typed. The Claims domain must never represent
 * semantic claim values using {@code Object}, arbitrary maps, JSON nodes, or
 * transport-layer structures.</p>
 *
 * <p>The currently supported forms intentionally cover the primitive semantic
 * values needed by the initial Claims model. Additional forms should be added
 * only when a concrete claim semantic requires them.</p>
 */
public sealed interface ClaimObject
        permits ClaimObject.Reference,
        ClaimObject.Text,
        ClaimObject.NumberValue,
        ClaimObject.BooleanValue,
        ClaimObject.TimestampValue,
        ClaimObject.Identifier {

    /**
     * Represents a reference-valued claim object.
     *
     * <p>The reference does not imply successful canonical entity resolution.</p>
     *
     * @param value referenced claim-level identity
     */
    record Reference(
            ClaimReference value
    ) implements ClaimObject {

        public Reference {
            Objects.requireNonNull(
                    value,
                    "value must not be null"
            );
        }
    }

    /**
     * Represents an exact textual claim value.
     *
     * <p>The value is preserved exactly. An empty string is structurally valid
     * because emptiness can itself be meaningful source data. Semantic
     * derivation rules may impose stronger constraints where appropriate.</p>
     *
     * @param value textual value
     */
    record Text(
            String value
    ) implements ClaimObject {

        public Text {
            Objects.requireNonNull(
                    value,
                    "value must not be null"
            );
        }
    }

    /**
     * Represents a semantic decimal number.
     *
     * <p>Equivalent decimal scales are canonicalized so that values such as
     * {@code 1.0} and {@code 1.00} are equal in the Claims value model.</p>
     *
     * @param value canonical decimal value
     */
    record NumberValue(
            BigDecimal value
    ) implements ClaimObject {

        public NumberValue {
            Objects.requireNonNull(
                    value,
                    "value must not be null"
            );

            value = canonicalize(value);
        }

        public static NumberValue of(String value) {
            Objects.requireNonNull(
                    value,
                    "value must not be null"
            );

            return new NumberValue(
                    new BigDecimal(value)
            );
        }

        private static BigDecimal canonicalize(
                BigDecimal value
        ) {
            if (value.signum() == 0) {
                return BigDecimal.ZERO;
            }

            return value.stripTrailingZeros();
        }
    }

    /**
     * Represents a boolean claim value.
     *
     * @param value boolean value
     */
    record BooleanValue(
            boolean value
    ) implements ClaimObject {
    }

    /**
     * Represents an exact point on the UTC timeline as a claim value.
     *
     * <p>This value is not a claim valid-time interval. It is the object of the
     * proposition itself, for example a timestamp asserted as the value of a
     * property.</p>
     *
     * @param value timestamp value
     */
    record TimestampValue(
            Timestamp value
    ) implements ClaimObject {

        public TimestampValue {
            Objects.requireNonNull(
                    value,
                    "value must not be null"
            );
        }
    }

    /**
     * Represents a structured identifier asserted as a value.
     *
     * <p>An identifier differs from a {@link Reference}. A reference identifies
     * a referent participating in the proposition, whereas an identifier is
     * itself the asserted value.</p>
     *
     * @param scheme identifier scheme or namespace
     * @param value  identifier value
     */
    record Identifier(
            String scheme,
            String value
    ) implements ClaimObject {

        public Identifier {
            scheme = requireNonBlank(
                    scheme,
                    "scheme"
            );

            value = requireNonBlank(
                    value,
                    "value"
            );
        }

        private static String requireNonBlank(
                String value,
                String name
        ) {
            Objects.requireNonNull(
                    value,
                    name + " must not be null"
            );

            if (value.isBlank()) {
                throw new IllegalArgumentException(
                        name + " must not be blank"
                );
            }

            return value;
        }
    }
}