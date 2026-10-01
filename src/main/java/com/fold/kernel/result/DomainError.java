package com.fold.kernel.result;

/**
 * Describes an expected domain-level failure.
 *
 * <p>A domain error represents a failure that callers may reasonably handle as
 * part of normal application behavior. Programming defects and unexpected
 * infrastructure failures are not domain errors.</p>
 */
public interface DomainError {

    /**
     * Returns a stable machine-readable error code.
     *
     * @return the error code
     */
    String code();

    /**
     * Returns a human-readable description of the failure.
     *
     * @return the error message
     */
    String message();
}