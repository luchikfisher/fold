package com.fold.modules.observations.api.view;

import java.util.Objects;

/**
 * Public read representation of one semantic observation field.
 *
 * @param name  semantic field name
 * @param value typed field value
 */
public record ObservationFieldView(
        String name,
        ObservationValueView value
) {

    public ObservationFieldView {
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(value, "value must not be null");
    }
}