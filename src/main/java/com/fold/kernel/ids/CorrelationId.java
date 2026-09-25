package com.fold.kernel.ids;

import java.util.Objects;
import java.util.UUID;

/**
 * Identifies a causal chain of related operations and events.
 *
 * <p>Events produced as part of the same logical chain retain the same
 * correlation identifier even when processing crosses transaction or module
 * boundaries.</p>
 *
 * @param value the underlying UUID
 */
public record CorrelationId(UUID value) implements DomainId<UUID> {

    public CorrelationId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static CorrelationId random() {
        return new CorrelationId(UUID.randomUUID());
    }

    public static CorrelationId from(String value) {
        Objects.requireNonNull(value, "value must not be null");

        if (value.isBlank()) {
            throw new IllegalArgumentException("value must not be blank");
        }

        return new CorrelationId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return asString();
    }
}