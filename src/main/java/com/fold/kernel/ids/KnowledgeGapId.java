package com.fold.kernel.ids;

import java.util.Objects;
import java.util.UUID;

/**
 * Identifies an explicit gap in FOLD's current knowledge state.
 *
 * @param value the underlying UUID
 */
public record KnowledgeGapId(UUID value) implements DomainId<UUID> {

    public KnowledgeGapId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static KnowledgeGapId random() {
        return new KnowledgeGapId(UUID.randomUUID());
    }

    public static KnowledgeGapId from(String value) {
        Objects.requireNonNull(value, "value must not be null");

        if (value.isBlank()) {
            throw new IllegalArgumentException("value must not be blank");
        }

        return new KnowledgeGapId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return asString();
    }
}