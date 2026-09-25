package com.fold.kernel.pagination;

import java.util.List;
import java.util.Objects;

/**
 * Requests one zero-based page of a bounded result set.
 *
 * @param page zero-based page index
 * @param size maximum number of items in the page
 * @param sort requested ordering
 */
public record PageRequest(
        int page,
        int size,
        List<Sort> sort
) {

    public PageRequest {
        if (page < 0) {
            throw new IllegalArgumentException(
                    "page must not be negative"
            );
        }

        if (size < 1) {
            throw new IllegalArgumentException(
                    "size must be greater than zero"
            );
        }

        Objects.requireNonNull(sort, "sort must not be null");

        if (sort.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException(
                    "sort must not contain null"
            );
        }

        sort = List.copyOf(sort);
    }

    public static PageRequest of(int page, int size) {
        return new PageRequest(page, size, List.of());
    }

    public static PageRequest of(
            int page,
            int size,
            List<Sort> sort
    ) {
        return new PageRequest(page, size, sort);
    }
}