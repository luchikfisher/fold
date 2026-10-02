package com.fold.modules.claims.domain.value;

/**
 * Represents the revision number of authoritative Claim aggregate state.
 *
 * <p>A revision does not permit mutation of the proposition itself. Claim
 * proposition semantics are immutable. Revision exists to version legitimate
 * aggregate-state evolution such as lifecycle transitions and related
 * metadata.</p>
 *
 * <p>The initial revision is {@code 1}.</p>
 *
 * @param value positive revision number
 */
public record ClaimRevision(
        long value
) implements Comparable<ClaimRevision> {

    public ClaimRevision {
        if (value < 1) {
            throw new IllegalArgumentException(
                    "value must be greater than zero"
            );
        }
    }

    public static ClaimRevision initial() {
        return new ClaimRevision(1);
    }

    /**
     * Returns the next revision.
     *
     * @throws ArithmeticException if the revision cannot be incremented
     */
    public ClaimRevision next() {
        return new ClaimRevision(
                Math.incrementExact(value)
        );
    }

    @Override
    public int compareTo(
            ClaimRevision other
    ) {
        return Long.compare(
                value,
                other.value
        );
    }
}