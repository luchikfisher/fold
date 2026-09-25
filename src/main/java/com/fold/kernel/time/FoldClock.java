package com.fold.kernel.time;

/**
 * Provides the current time to FOLD domain and application code.
 *
 * <p>Code whose behavior depends on the present time must obtain it through
 * this abstraction rather than reading the operating-system clock directly.
 * This keeps time-dependent behavior deterministic and testable.</p>
 */
@FunctionalInterface
public interface FoldClock {

    /**
     * Returns the current timestamp.
     *
     * @return the current timestamp
     */
    Timestamp now();
}