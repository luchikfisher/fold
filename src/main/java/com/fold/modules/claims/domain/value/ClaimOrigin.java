package com.fold.modules.claims.domain.value;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Describes the upstream artifacts associated with the provenance of a claim.
 *
 * <p>Origin and derivation are deliberately separate concepts:</p>
 *
 * <ul>
 *     <li>origin answers which upstream artifacts contributed to the claim;</li>
 *     <li>derivation answers how the claim was produced.</li>
 * </ul>
 *
 * <p>An origin may temporarily contain no references. This is intentional:
 * some claim-production paths, such as future analyst-entered assertions, may
 * have provenance represented by mechanisms that do not yet exist in the
 * kernel. The Claim aggregate may impose stronger requirements for specific
 * derivation kinds when those semantics are introduced.</p>
 *
 * @param references immutable set of upstream provenance references
 */
public record ClaimOrigin(
        Set<ClaimOriginReference> references
) {

    public ClaimOrigin {
        Objects.requireNonNull(
                references,
                "references must not be null"
        );

        if (references.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException(
                    "references must not contain null"
            );
        }

        references = Collections.unmodifiableSet(
                new LinkedHashSet<>(references)
        );
    }

    /**
     * Creates an origin with no currently represented upstream references.
     */
    public static ClaimOrigin none() {
        return new ClaimOrigin(Set.of());
    }

    /**
     * Creates an origin from one or more provenance references.
     */
    public static ClaimOrigin of(
            ClaimOriginReference first,
            ClaimOriginReference... additional
    ) {
        Objects.requireNonNull(
                first,
                "first must not be null"
        );
        Objects.requireNonNull(
                additional,
                "additional must not be null"
        );

        LinkedHashSet<ClaimOriginReference> references =
                new LinkedHashSet<>();

        references.add(first);

        Arrays.stream(additional).forEach(reference -> {
            if (reference == null) {
                throw new IllegalArgumentException(
                        "additional must not contain null"
                );
            }

            references.add(reference);
        });

        return new ClaimOrigin(references);
    }

    public boolean isEmpty() {
        return references.isEmpty();
    }

    public boolean contains(
            ClaimOriginReference reference
    ) {
        Objects.requireNonNull(
                reference,
                "reference must not be null"
        );

        return references.contains(reference);
    }

    public int size() {
        return references.size();
    }
}