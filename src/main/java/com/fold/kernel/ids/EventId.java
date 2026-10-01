package com.fold.kernel.ids;

import java.util.Objects;
import java.util.UUID;

/**
 * Identifies one emitted event instance.
 *
 * <p>The identifier belongs to the emission itself, not to the domain object
 * described by the event.</p>
 *
 * @param value the underlying UUID
 */
public record EventId(UUID value) implements DomainId<UUID> {

    public EventId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static EventId random() {
        return new EventId(UUID.randomUUID());
    }

    public static EventId from(String value) {
        Objects.requireNonNull(value, "value must not be null");

        if (value.isBlank()) {
            throw new IllegalArgumentException("value must not be blank");
        }

        return new EventId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return asString();
    }
}