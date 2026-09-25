package com.fold.kernel.ids;

import java.util.Objects;
import java.util.UUID;

/**
 * Identifies an observation accepted into FOLD.
 *
 * @param value the underlying UUID
 */
public record ObservationId(UUID value) implements DomainId<UUID> {

    public ObservationId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static ObservationId random() {
        return new ObservationId(UUID.randomUUID());
    }

    public static ObservationId from(String value) {
        Objects.requireNonNull(value, "value must not be null");

        if (value.isBlank()) {
            throw new IllegalArgumentException("value must not be blank");
        }

        return new ObservationId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return asString();
    }
}