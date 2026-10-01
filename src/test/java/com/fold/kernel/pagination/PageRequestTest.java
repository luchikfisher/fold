package com.fold.kernel.pagination;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PageRequestTest {

    @Test
    void shouldCreateUnsortedPageRequest() {
        PageRequest request =
                PageRequest.of(0, 50);

        assertThat(request.page()).isZero();
        assertThat(request.size()).isEqualTo(50);
        assertThat(request.sort()).isEmpty();
    }

    @Test
    void shouldCreateSortedPageRequest() {
        PageRequest request =
                PageRequest.of(
                        1,
                        20,
                        List.of(
                                Sort.descending("confidence")
                        )
                );

        assertThat(request.page()).isEqualTo(1);
        assertThat(request.sort())
                .containsExactly(
                        Sort.descending("confidence")
                );
    }

    @Test
    void sortListShouldBeDefensivelyCopied() {
        List<Sort> source = new ArrayList<>();
        source.add(Sort.ascending("name"));

        PageRequest request =
                PageRequest.of(0, 10, source);

        source.clear();

        assertThat(request.sort())
                .hasSize(1);
    }

    @Test
    void exposedSortListShouldBeImmutable() {
        PageRequest request =
                PageRequest.of(
                        0,
                        10,
                        List.of(Sort.ascending("name"))
                );

        assertThatThrownBy(
                () -> request.sort()
                        .add(Sort.ascending("createdAt"))
        )
                .isInstanceOf(
                        UnsupportedOperationException.class
                );
    }

    @Test
    void shouldRejectNegativePage() {
        assertThatThrownBy(
                () -> PageRequest.of(-1, 10)
        )
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectZeroSize() {
        assertThatThrownBy(
                () -> PageRequest.of(0, 0)
        )
                .isInstanceOf(IllegalArgumentException.class);
    }
}