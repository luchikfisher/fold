package com.fold.modules.claims.domain.value;

import java.util.Objects;

/**
 * Represents the subject about which a claim makes an assertion.
 *
 * <p>The subject belongs to the Claims semantic model and does not imply a
 * canonical FOLD entity. In particular, it must remain usable before identity
 * resolution has occurred.</p>
 *
 * @param reference reference identifying the asserted subject
 */
public record ClaimSubject(
        ClaimReference reference
) {

    public ClaimSubject {
        Objects.requireNonNull(
                reference,
                "reference must not be null"
        );
    }

    /**
     * Creates a subject directly from a reference namespace and value.
     *
     * @param namespace reference namespace
     * @param value     reference value
     * @return claim subject
     */
    public static ClaimSubject of(
            String namespace,
            String value
    ) {
        return new ClaimSubject(
                new ClaimReference(namespace, value)
        );
    }
}