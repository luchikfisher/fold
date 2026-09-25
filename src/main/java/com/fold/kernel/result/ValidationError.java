package com.fold.kernel.result;

import java.util.Objects;

/**
 * Describes a violation of an input or domain invariant.
 *
 * @param code the stable machine-readable error code
 * @param message the human-readable description
 */
public record ValidationError(
        String code,
        String message
) implements DomainError {

    public ValidationError {
        Objects.requireNonNull(code, "code must not be null");
        Objects.requireNonNull(message, "message must not be null");

        if (code.isBlank()) {
            throw new IllegalArgumentException(
                    "code must not be blank"
            );
        }

        if (message.isBlank()) {
            throw new IllegalArgumentException(
                    "message must not be blank"
            );
        }
    }
}