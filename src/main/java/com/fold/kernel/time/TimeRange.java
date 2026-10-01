package com.fold.kernel.time;

import java.util.Objects;
import java.util.Optional;

/**
 * Represents a half-open interval of time using {@code [start, end)} semantics.
 *
 * <p>The start belongs to the range. A finite end does not. Therefore two
 * ranges such as {@code [10:00, 11:00)} and {@code [11:00, 12:00)} are
 * adjacent but do not overlap.</p>
 *
 * <p>An absent end represents an interval that continues indefinitely.</p>
 *
 * @param startInclusive the inclusive beginning of the range
 * @param endExclusive   the exclusive end, or {@code null} for an open-ended range
 */
public record TimeRange(
        Timestamp startInclusive,
        Timestamp endExclusive
) {

    public TimeRange {
        Objects.requireNonNull(
                startInclusive,
                "startInclusive must not be null"
        );

        if (endExclusive != null
                && !startInclusive.isBefore(endExclusive)) {
            throw new IllegalArgumentException(
                    "startInclusive must be before endExclusive"
            );
        }
    }

    public static TimeRange between(
            Timestamp startInclusive,
            Timestamp endExclusive
    ) {
        return new TimeRange(startInclusive, endExclusive);
    }

    public static TimeRange from(Timestamp startInclusive) {
        return new TimeRange(startInclusive, null);
    }

    public Optional<Timestamp> end() {
        return Optional.ofNullable(endExclusive);
    }

    public boolean isOpenEnded() {
        return endExclusive == null;
    }

    public boolean contains(Timestamp timestamp) {
        Objects.requireNonNull(timestamp, "timestamp must not be null");

        if (timestamp.isBefore(startInclusive)) {
            return false;
        }

        return endExclusive == null || timestamp.isBefore(endExclusive);
    }

    public boolean overlaps(TimeRange other) {
        Objects.requireNonNull(other, "other must not be null");

        boolean thisStartsBeforeOtherEnds =
                other.endExclusive == null
                        || startInclusive.isBefore(other.endExclusive);

        boolean otherStartsBeforeThisEnds =
                endExclusive == null
                        || other.startInclusive.isBefore(endExclusive);

        return thisStartsBeforeOtherEnds
                && otherStartsBeforeThisEnds;
    }
}