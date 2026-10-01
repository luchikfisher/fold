package com.fold.kernel.ids;

import java.util.Objects;
import java.util.UUID;

/**
 * Identifies a recorded human or system decision.
 *
 * @param value the underlying UUID
 */
public record DecisionId(UUID value) implements DomainId<UUID> {

    public DecisionId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static DecisionId random() {
        return new DecisionId(UUID.randomUUID());
    }

    public static DecisionId from(String value) {
        Objects.requireNonNull(value, "value must not be null");

        if (value.isBlank()) {
            throw new IllegalArgumentException("value must not be blank");
        }

        return new DecisionId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return asString();
    }
}