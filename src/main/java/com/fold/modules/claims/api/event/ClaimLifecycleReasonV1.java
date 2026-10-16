package com.fold.modules.claims.api.event;

import java.util.Objects;

/** Stable public reason representation, independent of Claims domain types. */
public record ClaimLifecycleReasonV1(String code, String message) {
    public ClaimLifecycleReasonV1 {
        Objects.requireNonNull(code, "code must not be null");
        Objects.requireNonNull(message, "message must not be null");
        if (code.isBlank() || message.isBlank()) {
            throw new IllegalArgumentException("reason code and message must not be blank");
        }
    }
}
