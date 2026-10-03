package com.fold.modules.claims.domain.derivation;

import com.fold.kernel.time.Timestamp;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * Represents a typed semantic value available to a Claim derivation rule.
 *
 * <p>This type represents derivation input, not a Claim object. A derivation
 * rule must explicitly decide how an input value contributes to zero, one, or
 * many Claim propositions.</p>
 *
 * <p>In particular, a {@link ListValue} must not be automatically converted
 * into a Claim object merely because the upstream Observation contained a
 * list.</p>
 */
public sealed interface ClaimDerivationValue
        permits ClaimDerivationValue.Text,
        ClaimDerivationValue.NumberValue,
        ClaimDerivationValue.BooleanValue,
        ClaimDerivationValue.TimestampValue,
        ClaimDerivationValue.Identifier,
        ClaimDerivationValue.ListValue {

    record Text(
            String value
    ) implements ClaimDerivationValue {

        public Text {
            Objects.requireNonNull(
                    value,
                    "value must not be null"
            );
        }
    }

    record NumberValue(
            BigDecimal value
    ) implements ClaimDerivationValue {

        public NumberValue {
            Objects.requireNonNull(
                    value,
                    "value must not be null"
            );
        }
    }

    record BooleanValue(
            boolean value
    ) implements ClaimDerivationValue {
    }

    record TimestampValue(
            Timestamp value
    ) implements ClaimDerivationValue {

        public TimestampValue {
            Objects.requireNonNull(
                    value,
                    "value must not be null"
            );
        }
    }

    record Identifier(
            String scheme,
            String value
    ) implements ClaimDerivationValue {

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

    record ListValue(
            List<ClaimDerivationValue> values
    ) implements ClaimDerivationValue {

        public ListValue {
            Objects.requireNonNull(
                    values,
                    "values must not be null"
            );

            if (values.stream().anyMatch(Objects::isNull)) {
                throw new IllegalArgumentException(
                        "values must not contain null"
                );
            }

            values = List.copyOf(values);
        }
    }
}