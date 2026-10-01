package com.fold.modules.observations.domain.model;

import com.fold.modules.observations.domain.value.ObservationValue;

import java.util.Objects;

/**
 * Associates one semantic field name with one typed observation value.
 *
 * <p>Field names are semantic names produced by upstream normalization.
 * They are not source column names and are not tied to a persistence schema.</p>
 *
 * <p>One payload may contain each field name at most once. Multi-valued
 * semantic fields are represented using
 * {@link ObservationValue.ListValue} rather than duplicate fields.</p>
 *
 * @param name  the semantic field name
 * @param value the observed semantic value
 */
public record ObservationField(
        String name,
        ObservationValue value
) {

    public ObservationField {
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(value, "value must not be null");

        if (name.isBlank()) {
            throw new IllegalArgumentException(
                    "name must not be blank"
            );
        }

        if (!name.equals(name.trim())) {
            throw new IllegalArgumentException(
                    "name must not contain leading or trailing whitespace"
            );
        }
    }

    public static ObservationField of(
            String name,
            ObservationValue value
    ) {
        return new ObservationField(name, value);
    }
}