package com.fold.modules.claims.domain.value;

import java.util.Objects;

/**
 * Describes the reason for an explicit Claim lifecycle decision.
 *
 * <p>The code provides stable machine-readable meaning while the message
 * provides a human-readable explanation.</p>
 *
 * <p>This value does not attempt to model all future decision provenance.
 * Actor identity, review workflow, authorization, and decision records belong
 * to their respective bounded contexts when introduced.</p>
 *
 * @param code    stable machine-readable reason code
 * @param message human-readable explanation
 */
public record ClaimLifecycleReason(
        String code,
        String message
) {

    public ClaimLifecycleReason {
        code = requireNonBlank(code, "code");
        message = requireNonBlank(message, "message");
    }

    private static String requireNonBlank(
            String value,
            String name
    ) {
        Objects.requireNonNull(
                value,
                name + " must not be null"
        );

        if (value.isBlank()) {
            throw new IllegalArgumentException(
                    name + " must not be blank"
            );
        }

        return value;
    }
}