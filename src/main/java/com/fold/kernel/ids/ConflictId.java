package com.fold.kernel.ids;

import java.util.Objects;
import java.util.UUID;

/**
 * Identifies a recorded knowledge conflict.
 *
 * @param value the underlying UUID
 */
public record ConflictId(UUID value) implements DomainId<UUID> {

    public ConflictId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static ConflictId random() {
        return new ConflictId(UUID.randomUUID());
    }

    public static ConflictId from(String value) {
        Objects.requireNonNull(value, "value must not be null");

        if (value.isBlank()) {
            throw new IllegalArgumentException("value must not be blank");
        }

        return new ConflictId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return asString();
    }
}