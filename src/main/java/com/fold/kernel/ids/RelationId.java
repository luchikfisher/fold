package com.fold.kernel.ids;

import java.util.Objects;
import java.util.UUID;

/**
 * Identifies a relation represented by FOLD.
 *
 * @param value the underlying UUID
 */
public record RelationId(UUID value) implements DomainId<UUID> {

    public RelationId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static RelationId random() {
        return new RelationId(UUID.randomUUID());
    }

    public static RelationId from(String value) {
        Objects.requireNonNull(value, "value must not be null");

        if (value.isBlank()) {
            throw new IllegalArgumentException("value must not be blank");
        }

        return new RelationId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return asString();
    }
}