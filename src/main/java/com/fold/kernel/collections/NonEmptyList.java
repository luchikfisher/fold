package com.fold.kernel.collections;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * An immutable list guaranteed to contain at least one element.
 *
 * <p>This type allows domain APIs to express non-emptiness as part of the type
 * contract rather than requiring repeated runtime checks by every consumer.</p>
 *
 * @param values the contained values
 * @param <T> element type
 */
public record NonEmptyList<T>(List<T> values)
        implements Iterable<T> {

    public NonEmptyList {
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

        values = List.copyOf(values);
    }

    public static <T> NonEmptyList<T> of(T first) {
        Objects.requireNonNull(first, "first must not be null");
        return new NonEmptyList<>(List.of(first));
    }

    @SafeVarargs
    public static <T> NonEmptyList<T> of(
            T first,
            T... remaining
    ) {
        Objects.requireNonNull(first, "first must not be null");
        Objects.requireNonNull(
                remaining,
                "remaining must not be null"
        );

        List<T> values =
                new ArrayList<>(remaining.length + 1);

        values.add(first);

        for (T value : remaining) {
            values.add(
                    Objects.requireNonNull(
                            value,
                            "values must not contain null"
                    )
            );
        }

        return new NonEmptyList<>(values);
    }

    public T first() {
        return values.getFirst();
    }

    public T get(int index) {
        return values.get(index);
    }

    public int size() {
        return values.size();
    }

    public Stream<T> stream() {
        return values.stream();
    }

    public List<T> asList() {
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