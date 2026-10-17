package com.fold.modules.claims.api.view;

import com.fold.kernel.time.Timestamp;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Public typed representation of a Claim object.
 */
public sealed interface ClaimObjectView
        permits ClaimObjectView.Reference,
        ClaimObjectView.Text,
        ClaimObjectView.NumberValue,
        ClaimObjectView.BooleanValue,
        ClaimObjectView.TimestampValue,
        ClaimObjectView.Identifier {

    record Reference(
            String namespace,
            String value
    ) implements ClaimObjectView {

        public Reference {
            Objects.requireNonNull(namespace);
            Objects.requireNonNull(value);
        }
    }

    record Text(
            String value
    ) implements ClaimObjectView {

        public Text {
            Objects.requireNonNull(value);
        }
    }

    record NumberValue(
            BigDecimal value
    ) implements ClaimObjectView {

        public NumberValue {
            Objects.requireNonNull(value);
        }
    }

    record BooleanValue(
            boolean value
    ) implements ClaimObjectView {
    }

    record TimestampValue(
            Timestamp value
    ) implements ClaimObjectView {

        public TimestampValue {
            Objects.requireNonNull(value);
        }
    }

    record Identifier(
            String scheme,
            String value
    ) implements ClaimObjectView {

        public Identifier {
            Objects.requireNonNull(scheme);
            Objects.requireNonNull(value);
        }
    }
}