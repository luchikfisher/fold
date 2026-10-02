package com.fold.modules.observations.domain.value;

import java.util.Locale;
import java.util.Objects;

/**
 * Identifies the canonical semantic content of an observation for
 * deterministic duplicate detection.
 *
 * <p>A fingerprint is not the identity of an observation aggregate. Multiple
 * submissions may resolve to the same fingerprint and therefore to the same
 * previously stored observation.</p>
 *
 * <p>The fingerprint contains both the cryptographic algorithm and the
 * canonicalization version. The version is part of the semantic contract:
 * fingerprints produced by different canonicalization versions must never be
 * assumed to be directly comparable.</p>
 *
 * @param version   the canonical fingerprint scheme version
 * @param algorithm the cryptographic digest algorithm
 * @param value     the algorithm-specific digest value
 */
public record ObservationFingerprint(
        int version,
        String algorithm,
        String value
) {

    public static final int CURRENT_VERSION = 1;
    public static final String SHA_256 = "SHA-256";

    public ObservationFingerprint {
        if (version < 1) {
            throw new IllegalArgumentException(
                    "version must be greater than zero"
            );
        }

        Objects.requireNonNull(
                algorithm,
                "algorithm must not be null"
        );

        Objects.requireNonNull(
                value,
                "value must not be null"
        );

        if (algorithm.isBlank()) {
            throw new IllegalArgumentException(
                    "algorithm must not be blank"
            );
        }

        if (value.isBlank()) {
            throw new IllegalArgumentException(
                    "value must not be blank"
            );
        }

        if (!algorithm.equals(algorithm.trim())) {
            throw new IllegalArgumentException(
                    "algorithm must not contain leading or trailing whitespace"
            );
        }

        if (!value.equals(value.trim())) {
            throw new IllegalArgumentException(
                    "value must not contain leading or trailing whitespace"
            );
        }

        if (SHA_256.equals(algorithm)) {
            value = value.toLowerCase(Locale.ROOT);

            if (!value.matches("[0-9a-f]{64}")) {
                throw new IllegalArgumentException(
                        "SHA-256 fingerprint must contain exactly 64 hexadecimal characters"
                );
            }
        }
    }

    /**
     * Creates a version-one SHA-256 observation fingerprint.
     *
     * @param hexadecimalDigest a 64-character hexadecimal SHA-256 digest
     * @return the fingerprint
     */
    public static ObservationFingerprint sha256V1(
            String hexadecimalDigest
    ) {
        return sha256(
                CURRENT_VERSION,
                hexadecimalDigest
        );
    }

    /**
     * Creates a versioned SHA-256 observation fingerprint.
     *
     * @param version            the canonicalization version
     * @param hexadecimalDigest a 64-character hexadecimal SHA-256 digest
     * @return the fingerprint
     */
    public static ObservationFingerprint sha256(
            int version,
            String hexadecimalDigest
    ) {
        return new ObservationFingerprint(
                version,
                SHA_256,
                hexadecimalDigest
        );
    }

    public boolean isSha256() {
        return SHA_256.equals(algorithm);
    }

    @Override
    public String toString() {
        return "v%d:%s:%s".formatted(
                version,
                algorithm,
                value
        );
    }
}