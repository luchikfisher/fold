package com.fold.kernel.ids;

import java.util.Objects;
import java.util.UUID;

/**
 * Identifies a semantic claim recorded by FOLD.
 *
 * @param value the underlying UUID
 */
public record ClaimId(UUID value) implements DomainId<UUID> {

    public ClaimId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static ClaimId random() {
        return new ClaimId(UUID.randomUUID());
    }

    public static ClaimId from(String value) {
        Objects.requireNonNull(value, "value must not be null");

        if (value.isBlank()) {
            throw new IllegalArgumentException("value must not be blank");
        }

        return new ClaimId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return asString();
    }
}