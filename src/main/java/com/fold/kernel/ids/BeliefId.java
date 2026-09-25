package com.fold.kernel.ids;

import java.util.Objects;
import java.util.UUID;

/**
 * Identifies a belief maintained by FOLD.
 *
 * @param value the underlying UUID
 */
public record BeliefId(UUID value) implements DomainId<UUID> {

    public BeliefId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static BeliefId random() {
        return new BeliefId(UUID.randomUUID());
    }

    public static BeliefId from(String value) {
        Objects.requireNonNull(value, "value must not be null");

        if (value.isBlank()) {
            throw new IllegalArgumentException("value must not be blank");
        }

        return new BeliefId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return asString();
    }
}