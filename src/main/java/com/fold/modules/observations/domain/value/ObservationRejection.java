package com.fold.modules.observations.domain.value;

import java.util.Objects;

/**
 * Describes why an observation was rejected from downstream knowledge
 * processing.
 *
 * <p>The code is a stable machine-readable identifier. The message explains
 * the rejection to operators and developers.</p>
 *
 * <p>A rejection describes the acceptance decision only. It does not erase the
 * observation; rejected observations remain available for provenance,
 * diagnostics, and possible future reprocessing.</p>
 *
 * @param code    the stable rejection code
 * @param message the human-readable explanation
 */
public record ObservationRejection(
        String code,
        String message
) {

    public ObservationRejection {
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

        if (!code.equals(code.trim())) {
            throw new IllegalArgumentException(
                    "code must not contain leading or trailing whitespace"
            );
        }

        if (!message.equals(message.trim())) {
            throw new IllegalArgumentException(
                    "message must not contain leading or trailing whitespace"
            );
        }
    }

    public static ObservationRejection of(
            String code,
            String message
    ) {
        return new ObservationRejection(code, message);
    }
}