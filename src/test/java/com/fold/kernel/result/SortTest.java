package com.fold.kernel.pagination;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SortTest {

    @Test
    void ascendingFactoryShouldCreateAscendingSort() {
        Sort sort = Sort.ascending("createdAt");

        assertThat(sort.field()).isEqualTo("createdAt");
        assertThat(sort.direction())
                .isEqualTo(SortDirection.ASCENDING);
    }

    @Test
    void descendingFactoryShouldCreateDescendingSort() {
        Sort sort = Sort.descending("confidence");

        assertThat(sort.direction())
                .isEqualTo(SortDirection.DESCENDING);
    }

    @Test
    void shouldRejectBlankField() {
        assertThatThrownBy(
                () -> Sort.ascending(" ")
        )
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectNullDirection() {
        assertThatThrownBy(
                () -> new Sort("name", null)
        )
                .isInstanceOf(NullPointerException.class);
    }
}