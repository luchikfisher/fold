package com.fold.modules.claims.domain.value;

import com.fold.kernel.probability.Probability;

import java.util.Objects;

/**
 * Represents confidence associated with this particular claim assertion or
 * the derivation that produced it.
 *
 * <p>Claim confidence must not be interpreted as FOLD's aggregate confidence
 * that the proposition is true. Aggregate epistemic assessment belongs to
 * higher-level concepts such as Belief.</p>
 *
 * <p>For example, a claim extractor may be highly confident that a document
 * asserts a particular relationship even while FOLD has weak overall
 * confidence that the relationship is true.</p>
 *
 * @param value normalized confidence value
 */
public record ClaimConfidence(
        Probability value
) implements Comparable<ClaimConfidence> {

    public static final ClaimConfidence NONE =
            new ClaimConfidence(Probability.ZERO);

    public static final ClaimConfidence CERTAIN =
            new ClaimConfidence(Probability.ONE);

    public ClaimConfidence {
        Objects.requireNonNull(
                value,
                "value must not be null"
        );
    }

    public static ClaimConfidence of(double value) {
        return new ClaimConfidence(
                Probability.of(value)
        );
    }

    public double decimal() {
        return value.value();
    }

    public double percentage() {
        return value.percentage();
    }

    @Override
    public int compareTo(
            ClaimConfidence other
    ) {
        Objects.requireNonNull(
                other,
                "other must not be null"
        );

        return value.compareTo(other.value);
    }
}