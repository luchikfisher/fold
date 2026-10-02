package com.fold.modules.observations.domain.value;

import java.util.Objects;

/**
 * Identifies the semantic kind of an observation.
 *
 * <p>An observation type describes what kind of information was observed,
 * independently of the source that supplied it and independently of any
 * canonical entity that may later be inferred from it.</p>
 *
 * <p>Examples include {@code person-profile},
 * {@code organization-registration}, {@code ownership-record}, and
 * {@code transaction}.</p>
 *
 * <p>The value is intentionally open rather than represented by an enum.
 * FOLD must be able to accept new semantic observation types without changing
 * the observations domain model.</p>
 *
 * @param value the stable semantic type identifier
 */
public record ObservationType(String value) {

    public ObservationType {
        Objects.requireNonNull(value, "value must not be null");

        if (value.isBlank()) {
            throw new IllegalArgumentException(
                    "value must not be blank"
            );
        }

        if (!value.equals(value.trim())) {
            throw new IllegalArgumentException(
                    "value must not contain leading or trailing whitespace"
            );
        }
    }

    /**
     * Creates an observation type from its stable semantic identifier.
     *
     * @param value the type identifier
     * @return the observation type
     */
    public static ObservationType of(String value) {
        return new ObservationType(value);
    }

    @Override
    public String toString() {
        return value;
    }
}