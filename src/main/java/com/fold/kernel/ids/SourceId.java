package com.fold.kernel.ids;

import java.util.Objects;
import java.util.UUID;

/**
 * Identifies a source of information known to FOLD.
 *
 * @param value the underlying UUID
 */
public record SourceId(UUID value) implements DomainId<UUID> {

    public SourceId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static SourceId random() {
        return new SourceId(UUID.randomUUID());
    }

    public static SourceId from(String value) {
        Objects.requireNonNull(value, "value must not be null");

        if (value.isBlank()) {
            throw new IllegalArgumentException("value must not be blank");
        }

        return new SourceId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return asString();
    }
}