package com.fold.kernel.pagination;

import java.util.List;
import java.util.Objects;

/**
 * Represents one page of a larger result set.
 *
 * @param items items contained in this page
 * @param page zero-based page index
 * @param size configured page size
 * @param totalElements total number of matching elements
 * @param <T> item type
 */
public record Page<T>(
        List<T> items,
        int page,
        int size,
        long totalElements
) {

    public Page {
        Objects.requireNonNull(items, "items must not be null");

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

        if (totalElements < 0) {
            throw new IllegalArgumentException(
                    "totalElements must not be negative"
            );
        }

        if (items.size() > size) {
            throw new IllegalArgumentException(
                    "items must not exceed page size"
            );
        }

        if (items.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException(
                    "items must not contain null"
            );
        }

        items = List.copyOf(items);
    }

    public long totalPages() {
        return Math.ceilDiv(totalElements, size);
    }

    public boolean hasNext() {
        return page + 1L < totalPages();
    }

    public boolean hasPrevious() {
        return page > 0;
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }
}