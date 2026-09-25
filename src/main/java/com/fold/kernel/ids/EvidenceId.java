package com.fold.kernel.ids;

import java.util.Objects;
import java.util.UUID;

/**
 * Identifies an addressable piece of evidence.
 *
 * @param value the underlying UUID
 */
public record EvidenceId(UUID value) implements DomainId<UUID> {

    public EvidenceId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static EvidenceId random() {
        return new EvidenceId(UUID.randomUUID());
    }

    public static EvidenceId from(String value) {
        Objects.requireNonNull(value, "value must not be null");

        if (value.isBlank()) {
            throw new IllegalArgumentException("value must not be blank");
        }

        return new EvidenceId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return asString();
    }
}