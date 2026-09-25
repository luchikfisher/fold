package com.fold.kernel.pagination;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PageTest {

    @Test
    void shouldRepresentPage() {
        Page<String> page =
                new Page<>(
                        List.of("a", "b"),
                        0,
                        2,
                        5
                );

        assertThat(page.items())
                .containsExactly("a", "b");

        assertThat(page.page()).isZero();
        assertThat(page.size()).isEqualTo(2);
        assertThat(page.totalElements()).isEqualTo(5);
    }

    @Test
    void shouldCalculateTotalPages() {
        Page<String> page =
                new Page<>(
                        List.of("a", "b"),
                        0,
                        2,
                        5
                );

        assertThat(page.totalPages())
                .isEqualTo(3);
    }

    @Test
    void emptyResultShouldHaveZeroPages() {
        Page<String> page =
                new Page<>(
                        List.of(),
                        0,
                        20,
                        0
                );

        assertThat(page.totalPages()).isZero();
        assertThat(page.isEmpty()).isTrue();
    }

    @Test
    void shouldReportNextPage() {
        Page<String> page =
                new Page<>(
                        List.of("a", "b"),
                        0,
                        2,
                        5
                );

        assertThat(page.hasNext()).isTrue();
        assertThat(page.hasPrevious()).isFalse();
    }

    @Test
    void middlePageShouldHavePreviousAndNext() {
        Page<String> page =
                new Page<>(
                        List.of("c", "d"),
                        1,
                        2,
                        5
                );

        assertThat(page.hasPrevious()).isTrue();
        assertThat(page.hasNext()).isTrue();
    }

    @Test
    void lastPageShouldHaveNoNext() {
        Page<String> page =
                new Page<>(
                        List.of("e"),
                        2,
                        2,
                        5
                );

        assertThat(page.hasNext()).isFalse();
        assertThat(page.hasPrevious()).isTrue();
    }

    @Test
    void itemsShouldBeDefensivelyCopied() {
        List<String> source =
                new ArrayList<>(List.of("a"));

        Page<String> page =
                new Page<>(source, 0, 10, 1);

        source.clear();

        assertThat(page.items())
                .containsExactly("a");
    }

    @Test
    void shouldRejectMoreItemsThanPageSize() {
        assertThatThrownBy(
                () -> new Page<>(
                        List.of("a", "b"),
                        0,
                        1,
                        2
                )
        )
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectNegativeTotal() {
        assertThatThrownBy(
                () -> new Page<>(
                        List.of(),
                        0,
                        10,
                        -1
                )
        )
                .isInstanceOf(IllegalArgumentException.class);
    }
}