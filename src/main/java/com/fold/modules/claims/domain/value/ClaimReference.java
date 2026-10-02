package com.fold.modules.claims.domain.value;

import java.util.Objects;

/**
 * Identifies a referent used by a claim before or independently of canonical
 * FOLD entity resolution.
 *
 * <p>A claim reference is deliberately not an {@code EntityId}. It preserves
 * the reference identity available at the time the claim was created without
 * asserting that FOLD has resolved that reference to a canonical entity.</p>
 *
 * <p>The namespace determines how the value should be interpreted. For
 * example:</p>
 *
 * <pre>
 * namespace = "company-registry"
 * value     = "12345678"
 * </pre>
 *
 * <p>Claims must not silently rewrite these values when later identity
 * resolution associates them with canonical entities.</p>
 *
 * @param namespace namespace in which the reference value is meaningful
 * @param value     reference value within that namespace
 */
public record ClaimReference(
        String namespace,
        String value
) {

    public ClaimReference {
        namespace = requireNonBlank(
                namespace,
                "namespace"
        );

        value = requireNonBlank(
                value,
                "value"
        );
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