package com.fold.kernel.collections;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NonEmptySetTest {

    @Test
    void shouldCreateSingleElementSet() {
        NonEmptySet<String> values =
                NonEmptySet.of("a");

        assertThat(values.size()).isEqualTo(1);
        assertThat(values.first()).isEqualTo("a");
    }

    @Test
    void shouldRemoveDuplicates() {
        NonEmptySet<String> values =
                NonEmptySet.of(
                        "a",
                        "b",
                        "a"
                );

        assertThat(values.size()).isEqualTo(2);

        assertThat(values)
                .containsExactly("a", "b");
    }

    @Test
    void shouldPreserveEncounterOrder() {
        LinkedHashSet<String> source =
                new LinkedHashSet<>();

        source.add("c");
        source.add("a");
        source.add("b");

        NonEmptySet<String> values =
                new NonEmptySet<>(source);

        assertThat(values)
                .containsExactly("c", "a", "b");
    }

    @Test
    void constructorShouldDefensivelyCopySourceSet() {
        LinkedHashSet<String> source =
                new LinkedHashSet<>();

        source.add("a");

        NonEmptySet<String> values =
                new NonEmptySet<>(source);

        source.add("b");

        assertThat(values)
                .containsExactly("a");
    }

    @Test
    void exposedSetShouldBeImmutable() {
        NonEmptySet<String> values =
                NonEmptySet.of("a");

        assertThatThrownBy(
                () -> values.asSet().add("b")
        )
                .isInstanceOf(
                        UnsupportedOperationException.class
                );
    }

    @Test
    void shouldRejectEmptySet() {
        assertThatThrownBy(
                () -> new NonEmptySet<String>(
                        Set.of()
                )
        )
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectNullElements() {
        LinkedHashSet<String> source =
                new LinkedHashSet<>();

        source.add("a");
        source.add(null);

        assertThatThrownBy(
                () -> new NonEmptySet<>(source)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "values must not contain null"
                );
    }

    @Test
    void containsShouldUseSetSemantics() {
        NonEmptySet<String> values =
                NonEmptySet.of("a", "b");

        assertThat(values.contains("b")).isTrue();
        assertThat(values.contains("c")).isFalse();
    }
}