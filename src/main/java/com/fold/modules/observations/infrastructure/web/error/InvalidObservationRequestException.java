package com.fold.modules.observations.infrastructure.web.error;

/**
 * Indicates that an HTTP observation submission cannot be translated into a
 * valid observations-domain command.
 */
public final class InvalidObservationRequestException
        extends RuntimeException {

    public InvalidObservationRequestException(
            String message
    ) {
        super(message);
    }

    public InvalidObservationRequestException(
            String message,
            Throwable cause
    ) {
        super(message, cause);
    }
}