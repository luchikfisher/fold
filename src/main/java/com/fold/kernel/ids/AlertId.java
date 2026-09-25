package com.fold.kernel.ids;

import java.util.Objects;
import java.util.UUID;

/**
 * Identifies an alert created by FOLD.
 *
 * @param value the underlying UUID
 */
public record AlertId(UUID value) implements DomainId<UUID> {

    public AlertId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static AlertId random() {
        return new AlertId(UUID.randomUUID());
    }

    public static AlertId from(String value) {
        Objects.requireNonNull(value, "value must not be null");

        if (value.isBlank()) {
            throw new IllegalArgumentException("value must not be blank");
        }

        return new AlertId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return asString();
    }
}