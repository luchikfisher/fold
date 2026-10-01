package com.fold.kernel.ids;

import java.util.Objects;
import java.util.UUID;

/**
 * Identifies a report managed by FOLD.
 *
 * @param value the underlying UUID
 */
public record ReportId(UUID value) implements DomainId<UUID> {

    public ReportId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static ReportId random() {
        return new ReportId(UUID.randomUUID());
    }

    public static ReportId from(String value) {
        Objects.requireNonNull(value, "value must not be null");

        if (value.isBlank()) {
            throw new IllegalArgumentException("value must not be blank");
        }

        return new ReportId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return asString();
    }
}