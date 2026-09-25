package com.fold.kernel.pagination;

import java.util.Objects;

/**
 * Represents an opaque position used for cursor-based pagination.
 *
 * <p>Consumers must not interpret or modify the cursor value. Its meaning is
 * owned entirely by the component that creates and consumes it.</p>
 *
 * @param value the opaque cursor value
 */
public record Cursor(String value) {

    public Cursor {
        Objects.requireNonNull(value, "value must not be null");

        if (value.isBlank()) {
            throw new IllegalArgumentException(
                    "value must not be blank"
            );
        }
    }

    @Override
    public String toString() {
        return value;
    }
}