package com.fold.kernel.ids;

import java.util.Objects;
import java.util.UUID;

/**
 * Identifies a canonical entity in FOLD.
 *
 * @param value the underlying UUID
 */
public record EntityId(UUID value) implements DomainId<UUID> {

    public EntityId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static EntityId random() {
        return new EntityId(UUID.randomUUID());
    }

    public static EntityId from(String value) {
        Objects.requireNonNull(value, "value must not be null");

        if (value.isBlank()) {
            throw new IllegalArgumentException("value must not be blank");
        }

        return new EntityId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return asString();
    }
}