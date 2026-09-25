package com.fold.kernel.collections;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NonEmptyListTest {

    @Test
    void shouldCreateSingleElementList() {
        NonEmptyList<String> values =
                NonEmptyList.of("a");

        assertThat(values.size()).isEqualTo(1);
        assertThat(values.first()).isEqualTo("a");
    }

    @Test
    void shouldPreserveElementOrder() {
        NonEmptyList<String> values =
                NonEmptyList.of("a", "b", "c");

        assertThat(values.asList())
                .containsExactly("a", "b", "c");
    }

    @Test
    void shouldSupportIndexedAccess() {
        NonEmptyList<String> values =
                NonEmptyList.of("a", "b");

        assertThat(values.get(1))
                .isEqualTo("b");
    }

    @Test
    void constructorShouldDefensivelyCopySourceList() {
        List<String> source =
                new ArrayList<>(List.of("a"));

        NonEmptyList<String> values =
                new NonEmptyList<>(source);

        source.add("b");

        assertThat(values.asList())
                .containsExactly("a");
    }

    @Test
    void exposedListShouldBeImmutable() {
        NonEmptyList<String> values =
                NonEmptyList.of("a");

        assertThatThrownBy(
                () -> values.asList().add("b")
        )
                .isInstanceOf(
                        UnsupportedOperationException.class
                );
    }

    @Test
    void shouldRejectEmptyList() {
        assertThatThrownBy(
                () -> new NonEmptyList<>(
                        List.of()
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("values must not be empty");
    }

    @Test
    void shouldRejectNullElement() {
        List<String> values =
                new ArrayList<>();

        values.add("a");
        values.add(null);

        assertThatThrownBy(
                () -> new NonEmptyList<>(values)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "values must not contain null"
                );
    }

    @Test
    void iteratorShouldExposeAllElements() {
        NonEmptyList<String> values =
                NonEmptyList.of("a", "b", "c");

        assertThat(values)
                .containsExactly("a", "b", "c");
    }
}