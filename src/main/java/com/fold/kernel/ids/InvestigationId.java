package com.fold.kernel.ids;

import java.util.Objects;
import java.util.UUID;

/**
 * Identifies a persistent investigation.
 *
 * @param value the underlying UUID
 */
public record InvestigationId(UUID value) implements DomainId<UUID> {

    public InvestigationId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static InvestigationId random() {
        return new InvestigationId(UUID.randomUUID());
    }

    public static InvestigationId from(String value) {
        Objects.requireNonNull(value, "value must not be null");

        if (value.isBlank()) {
            throw new IllegalArgumentException("value must not be blank");
        }

        return new InvestigationId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return asString();
    }
}