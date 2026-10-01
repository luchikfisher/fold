package com.fold.kernel.ids;

import java.util.Objects;
import java.util.UUID;

/**
 * A {@link DomainId} represented by a UUID.
 *
 * <p>This type provides UUID-based identity without requiring the rest of the
 * system to depend directly on UUID as the universal identifier format.</p>
 *
 * @param id the underlying UUID
 */
public record UuidDomainId(UUID id) implements DomainId {

    public UuidDomainId {
        Objects.requireNonNull(id, "id must not be null");
    }

    /**
     * Creates a new identifier using a randomly generated UUID.
     *
     * @return a new unique identifier
     */
    public static UuidDomainId random() {
        return new UuidDomainId(UUID.randomUUID());
    }

    /**
     * Parses a UUID-backed identifier from its canonical string representation.
     *
     * @param value the UUID string
     * @return the parsed identifier
     * @throws NullPointerException if {@code value} is null
     * @throws IllegalArgumentException if {@code value} is blank or is not a valid UUID
     */
    public static UuidDomainId from(String value) {
        Objects.requireNonNull(value, "value must not be null");

        if (value.isBlank()) {
            throw new IllegalArgumentException("value must not be blank");
        }

        return new UuidDomainId(UUID.fromString(value));
    }

    /**
     * Returns the canonical UUID string.
     *
     * @return the identifier value
     */
    @Override
    public String value() {
        return id.toString();
    }

    @Override
    public String toString() {
        return value();
    }
}