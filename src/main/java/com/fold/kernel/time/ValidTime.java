package com.fold.kernel.time;

import java.util.Objects;

/**
 * Represents when a fact is considered valid in the modeled world.
 *
 * <p>Valid time describes the world being modeled, not FOLD's knowledge of
 * that world. It answers questions such as "When was this relationship
 * actually in effect?"</p>
 *
 * @param range the period during which the fact is considered valid
 */
public record ValidTime(TimeRange range) {

    public ValidTime {
        Objects.requireNonNull(range, "range must not be null");
    }

    public static ValidTime between(
            Timestamp startInclusive,
            Timestamp endExclusive
    ) {
        return new ValidTime(
                TimeRange.between(startInclusive, endExclusive)
        );
    }

    public static ValidTime from(Timestamp startInclusive) {
        return new ValidTime(TimeRange.from(startInclusive));
    }

    public boolean contains(Timestamp timestamp) {
        return range.contains(timestamp);
    }

    public boolean overlaps(ValidTime other) {
        Objects.requireNonNull(other, "other must not be null");
        return range.overlaps(other.range);
    }
}