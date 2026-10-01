package com.fold.modules.observations.domain.model;

import java.util.Objects;

/**
 * Identifies the subject as it exists in the observation's source-facing
 * semantic context.
 *
 * <p>An observation subject is not a canonical FOLD entity. It identifies the
 * thing that the incoming observation appears to describe before entity
 * resolution has determined whether it corresponds to any existing entity.</p>
 *
 * <p>The external key should be stable within the semantic namespace in which
 * the subject was observed. It may later contribute evidence to identity
 * resolution, but observations do not perform that resolution themselves.</p>
 *
 * @param type        the semantic kind of subject
 * @param externalKey the source-facing or normalized subject key
 */
public record ObservationSubject(
        String type,
        String externalKey
) {

    public ObservationSubject {
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(
                externalKey,
                "externalKey must not be null"
        );

        if (type.isBlank()) {
            throw new IllegalArgumentException(
                    "type must not be blank"
            );
        }

        if (externalKey.isBlank()) {
            throw new IllegalArgumentException(
                    "externalKey must not be blank"
            );
        }

        if (!type.equals(type.trim())) {
            throw new IllegalArgumentException(
                    "type must not contain leading or trailing whitespace"
            );
        }

        if (!externalKey.equals(externalKey.trim())) {
            throw new IllegalArgumentException(
                    "externalKey must not contain leading or trailing whitespace"
            );
        }
    }

    public static ObservationSubject of(
            String type,
            String externalKey
    ) {
        return new ObservationSubject(type, externalKey);
    }
}