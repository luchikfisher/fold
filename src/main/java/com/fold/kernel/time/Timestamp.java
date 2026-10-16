package com.fold.kernel.time;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Represents one absolute point on the UTC timeline at microsecond precision.
 *
 * <p>Every construction path truncates finer precision to microseconds, never
 * rounds. Equality, ordering, and serialization therefore use the same
 * canonical value throughout FOLD, including persistence. Any finer source
 * precision belongs in raw/source evidence rather than in a timestamp.</p>
 *
 * <p>A timestamp carries no domain-specific temporal meaning. Concepts such as
 * when a fact was true or when FOLD knew it are represented by dedicated
 * temporal types.</p>
 *
 * @param value the represented instant, truncated to microseconds
 */
public record Timestamp(Instant value) implements Comparable<Timestamp> {

    public Timestamp {
        Objects.requireNonNull(value, "value must not be null");
        value = value.truncatedTo(ChronoUnit.MICROS);
    }

    public static Timestamp of(Instant value) {
        return new Timestamp(value);
    }

    public static Timestamp parse(String value) {
        Objects.requireNonNull(value, "value must not be null");

        if (value.isBlank()) {
            throw new IllegalArgumentException("value must not be blank");
        }

        return new Timestamp(Instant.parse(value));
    }

    public boolean isBefore(Timestamp other) {
        Objects.requireNonNull(other, "other must not be null");
        return value.isBefore(other.value);
    }

    public boolean isAfter(Timestamp other) {
        Objects.requireNonNull(other, "other must not be null");
        return value.isAfter(other.value);
    }

    @Override
    public int compareTo(Timestamp other) {
        Objects.requireNonNull(other, "other must not be null");
        return value.compareTo(other.value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
