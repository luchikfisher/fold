package com.fold.kernel.probability;

import java.util.Objects;

/**
 * Represents the degree of confidence assigned to an assessment.
 *
 * <p>Confidence uses the same numeric range as probability but has different
 * semantics. A confidence value describes strength of belief or assessment;
 * it must not automatically be interpreted as a calibrated probability.</p>
 *
 * @param value the underlying normalized value
 */
public record Confidence(Probability value)
        implements Comparable<Confidence> {

    public static final Confidence NONE =
            new Confidence(Probability.ZERO);

    public static final Confidence CERTAIN =
            new Confidence(Probability.ONE);

    public Confidence {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static Confidence of(double value) {
        return new Confidence(Probability.of(value));
    }

    public double decimal() {
        return value.value();
    }

    public double percentage() {
        return value.percentage();
    }

    @Override
    public int compareTo(Confidence other) {
        Objects.requireNonNull(other, "other must not be null");
        return value.compareTo(other.value);
    }
}