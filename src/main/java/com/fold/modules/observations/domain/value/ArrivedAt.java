package com.fold.modules.observations.domain.value;

import com.fold.kernel.time.Timestamp;

import java.util.Objects;

/**
 * Represents when FOLD received an observation.
 *
 * <p>Arrival time belongs to FOLD's processing history. It must not be used as
 * a substitute for the time reported by the source.</p>
 *
 * @param value the time at which FOLD received the observation
 */
public record ArrivedAt(Timestamp value)
        implements Comparable<ArrivedAt> {

    public ArrivedAt {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static ArrivedAt of(Timestamp value) {
        return new ArrivedAt(value);
    }

    @Override
    public int compareTo(ArrivedAt other) {
        Objects.requireNonNull(other, "other must not be null");
        return value.compareTo(other.value);
    }
}