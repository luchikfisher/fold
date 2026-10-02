package com.fold.modules.observations.api.view;

import com.fold.kernel.time.Timestamp;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * Public read representation of a normalized observation value.
 *
 * <p>The API representation is deliberately independent of the observations
 * domain implementation.</p>
 */
public sealed interface ObservationValueView
        permits ObservationValueView.Text,
        ObservationValueView.Number,
        ObservationValueView.BooleanValue,
        ObservationValueView.TimestampValue,
        ObservationValueView.Identifier,
        ObservationValueView.ListValue {

    record Text(String value)
            implements ObservationValueView {

        public Text {
            Objects.requireNonNull(value, "value must not be null");
        }
    }

    record Number(BigDecimal value)
            implements ObservationValueView {

        public Number {
            Objects.requireNonNull(value, "value must not be null");
        }
    }

    record BooleanValue(boolean value)
            implements ObservationValueView {
    }

    record TimestampValue(Timestamp value)
            implements ObservationValueView {

        public TimestampValue {
            Objects.requireNonNull(value, "value must not be null");
        }
    }

    record Identifier(
            String scheme,
            String value
    ) implements ObservationValueView {

        public Identifier {
            Objects.requireNonNull(scheme, "scheme must not be null");
            Objects.requireNonNull(value, "value must not be null");
        }
    }

    record ListValue(
            List<ObservationValueView> values
    ) implements ObservationValueView {

        public ListValue {
            Objects.requireNonNull(values, "values must not be null");
            values = List.copyOf(values);
        }
    }
}