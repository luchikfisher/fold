package com.fold.modules.observations.domain.value;

import com.fold.kernel.time.Timestamp;

import java.util.Objects;

/**
 * Represents when the source says the observed information applies or was
 * observed.
 *
 * <p>This is source-facing time. It is distinct from {@link ArrivedAt}, which
 * records when FOLD received the observation.</p>
 *
 * <p>No invariant requires observed time to precede arrival time. A source may
 * legitimately describe information concerning the future, and delayed data
 * may arrive long after it was observed.</p>
 *
 * @param value the source-reported observation time
 */
public record ObservedAt(Timestamp value)
        implements Comparable<ObservedAt> {

    public ObservedAt {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static ObservedAt of(Timestamp value) {
        return new ObservedAt(value);
    }

    @Override
    public int compareTo(ObservedAt other) {
        Objects.requireNonNull(other, "other must not be null");
        return value.compareTo(other.value);
    }
}