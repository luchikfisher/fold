package com.fold.kernel.probability;

/**
 * Represents a probability in the closed interval {@code [0, 1]}.
 *
 * @param value the probability value
 */
public record Probability(double value)
        implements Comparable<Probability> {

    public static final Probability ZERO =
            new Probability(0.0);

    public static final Probability ONE =
            new Probability(1.0);

    public Probability {
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException(
                    "probability must be finite"
            );
        }

        if (value < 0.0 || value > 1.0) {
            throw new IllegalArgumentException(
                    "probability must be between 0 and 1"
            );
        }
    }

    public static Probability of(double value) {
        return new Probability(value);
    }

    public double percentage() {
        return value * 100.0;
    }

    @Override
    public int compareTo(Probability other) {
        return Double.compare(value, other.value);
    }
}