package com.fold.kernel.probability;

/**
 * Represents a finite numeric score used for ordering or comparison.
 *
 * <p>A score has no universal probability or confidence semantics and is not
 * restricted to the interval {@code [0, 1]}. The component that produces the
 * score defines its interpretation.</p>
 *
 * @param value the finite score value
 */
public record Score(double value)
        implements Comparable<Score> {

    public Score {
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException(
                    "score must be finite"
            );
        }
    }

    public static Score of(double value) {
        return new Score(value);
    }

    @Override
    public int compareTo(Score other) {
        return Double.compare(value, other.value);
    }
}