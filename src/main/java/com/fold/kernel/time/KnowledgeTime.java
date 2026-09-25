package com.fold.kernel.time;

import java.util.Objects;

/**
 * Represents when a fact or belief was part of FOLD's knowledge state.
 *
 * <p>Knowledge time describes the system's own epistemic history. It is
 * independent of when the represented fact was valid in the modeled world.</p>
 *
 * <p>This distinction enables FOLD to answer both "What was true at time T?"
 * and "What did FOLD believe at time T?"</p>
 *
 * @param range the period during which the knowledge was present in FOLD
 */
public record KnowledgeTime(TimeRange range) {

    public KnowledgeTime {
        Objects.requireNonNull(range, "range must not be null");
    }

    public static KnowledgeTime between(
            Timestamp startInclusive,
            Timestamp endExclusive
    ) {
        return new KnowledgeTime(
                TimeRange.between(startInclusive, endExclusive)
        );
    }

    public static KnowledgeTime from(Timestamp startInclusive) {
        return new KnowledgeTime(TimeRange.from(startInclusive));
    }

    public boolean contains(Timestamp timestamp) {
        return range.contains(timestamp);
    }

    public boolean overlaps(KnowledgeTime other) {
        Objects.requireNonNull(other, "other must not be null");
        return range.overlaps(other.range);
    }
}