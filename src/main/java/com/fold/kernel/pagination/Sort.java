package com.fold.kernel.pagination;

import java.util.Objects;

/**
 * Defines ordering by one named field.
 *
 * <p>The field name belongs to the API or query contract interpreting this
 * value. Sort itself does not inspect domain objects.</p>
 *
 * @param field     the field to order by
 * @param direction the ordering direction
 */
public record Sort(
        String field,
        SortDirection direction
) {

    public Sort {
        Objects.requireNonNull(field, "field must not be null");
        Objects.requireNonNull(
                direction,
                "direction must not be null"
        );

        if (field.isBlank()) {
            throw new IllegalArgumentException(
                    "field must not be blank"
            );
        }
    }

    public static Sort ascending(String field) {
        return new Sort(field, SortDirection.ASCENDING);
    }

    public static Sort descending(String field) {
        return new Sort(field, SortDirection.DESCENDING);
    }
}