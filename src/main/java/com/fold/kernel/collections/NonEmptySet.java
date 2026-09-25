package com.fold.kernel.collections;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * An immutable set guaranteed to contain at least one element.
 *
 * <p>Iteration order is deterministic and follows the encounter order of the
 * supplied values.</p>
 *
 * @param values the contained values
 * @param <T> element type
 */
public record NonEmptySet<T>(Set<T> values)
        implements Iterable<T> {

    public NonEmptySet {
        Objects.requireNonNull(values, "values must not be null");

        if (values.isEmpty()) {
            throw new IllegalArgumentException(
                    "values must not be empty"
            );
        }

        if (values.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException(
                    "values must not contain null"
            );
        }

        values = Collections.unmodifiableSet(
                new LinkedHashSet<>(values)
        );
    }

    public static <T> NonEmptySet<T> of(T first) {
        Objects.requireNonNull(first, "first must not be null");

        LinkedHashSet<T> values = new LinkedHashSet<>();
        values.add(first);

        return new NonEmptySet<>(values);
    }

    @SafeVarargs
    public static <T> NonEmptySet<T> of(
            T first,
            T... remaining
    ) {
        Objects.requireNonNull(first, "first must not be null");
        Objects.requireNonNull(
                remaining,
                "remaining must not be null"
        );

        LinkedHashSet<T> values = new LinkedHashSet<>();
        values.add(first);

        for (T value : remaining) {
            values.add(
                    Objects.requireNonNull(
                            value,
                            "values must not contain null"
                    )
            );
        }

        return new NonEmptySet<>(values);
    }

    public T first() {
        return values.iterator().next();
    }

    public boolean contains(T value) {
        return values.contains(value);
    }

    public int size() {
        return values.size();
    }

    public Stream<T> stream() {
        return values.stream();
    }

    public Set<T> asSet() {
        return values;
    }

    @Override
    public Iterator<T> iterator() {
        return values.iterator();
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        values.forEach(action);
    }
}