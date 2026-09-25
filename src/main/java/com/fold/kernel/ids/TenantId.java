package com.fold.kernel.ids;

import java.util.Objects;
import java.util.UUID;

/**
 * Identifies an isolated tenant within FOLD.
 *
 * @param value the underlying UUID
 */
public record TenantId(UUID value) implements DomainId<UUID> {

    public TenantId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static TenantId random() {
        return new TenantId(UUID.randomUUID());
    }

    public static TenantId from(String value) {
        Objects.requireNonNull(value, "value must not be null");

        if (value.isBlank()) {
            throw new IllegalArgumentException("value must not be blank");
        }

        return new TenantId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return asString();
    }
}