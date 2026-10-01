package com.fold.modules.observations.domain.value;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ObservationFingerprintTest {

    private static final String SHA_256 =
            "0123456789abcdef"
                    + "0123456789abcdef"
                    + "0123456789abcdef"
                    + "0123456789abcdef";

    @Test
    void shouldCreateSha256Fingerprint() {
        ObservationFingerprint fingerprint =
                ObservationFingerprint.sha256V1(SHA_256);

        assertThat(fingerprint.algorithm())
                .isEqualTo("SHA-256");

        assertThat(fingerprint.value())
                .isEqualTo(SHA_256);

        assertThat(fingerprint.isSha256())
                .isTrue();
    }

    @Test
    void shouldNormalizeHexadecimalCase() {
        ObservationFingerprint fingerprint =
                ObservationFingerprint.sha256V1(
                        SHA_256.toUpperCase()
                );

        assertThat(fingerprint.value())
                .isEqualTo(SHA_256);
    }

    @Test
    void shouldRejectInvalidSha256Length() {
        assertThatThrownBy(
                () -> ObservationFingerprint.sha256V1(
                        "abcdef"
                )
        )
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectNonHexadecimalSha256() {
        String invalid =
                "z".repeat(64);

        assertThatThrownBy(
                () -> ObservationFingerprint.sha256V1(
                        invalid
                )
        )
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void genericFingerprintShouldRetainAlgorithm() {
        ObservationFingerprint fingerprint =
                new ObservationFingerprint(
                        7,
                        "TEST",
                        "abc123"
                );

        assertThat(fingerprint.version())
                .isEqualTo(7);

        assertThat(fingerprint.algorithm())
                .isEqualTo("TEST");

        assertThat(fingerprint.value())
                .isEqualTo("abc123");
    }
}