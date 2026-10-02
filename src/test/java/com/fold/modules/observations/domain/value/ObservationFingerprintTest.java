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

        assertThat(fingerprint.version())
                .isEqualTo(1);

        assertThat(fingerprint.algorithm())
                .isEqualTo("SHA-256");

        assertThat(fingerprint.value())
                .isEqualTo(SHA_256);

        assertThat(fingerprint.isSha256())
                .isTrue();
    }

    @Test
    void shouldNormalizeHexadecimalCaseThroughFactory() {
        ObservationFingerprint fingerprint =
                ObservationFingerprint.sha256V1(
                        SHA_256.toUpperCase()
                );

        assertThat(fingerprint.value())
                .isEqualTo(SHA_256);
    }

    @Test
    void shouldNormalizeHexadecimalCaseThroughCanonicalConstructor() {
        ObservationFingerprint fingerprint =
                new ObservationFingerprint(
                        1,
                        ObservationFingerprint.SHA_256,
                        SHA_256.toUpperCase()
                );

        assertThat(fingerprint.value())
                .isEqualTo(SHA_256);
    }

    @Test
    void shouldRejectInvalidSha256LengthThroughFactory() {
        assertThatThrownBy(
                () -> ObservationFingerprint.sha256V1(
                        "abcdef"
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "SHA-256 fingerprint must contain exactly 64 hexadecimal characters"
                );
    }

    @Test
    void shouldRejectInvalidSha256LengthThroughCanonicalConstructor() {
        assertThatThrownBy(
                () -> new ObservationFingerprint(
                        1,
                        ObservationFingerprint.SHA_256,
                        "abcdef"
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "SHA-256 fingerprint must contain exactly 64 hexadecimal characters"
                );
    }

    @Test
    void shouldRejectNonHexadecimalSha256ThroughFactory() {
        String invalid =
                "z".repeat(64);

        assertThatThrownBy(
                () -> ObservationFingerprint.sha256V1(
                        invalid
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "SHA-256 fingerprint must contain exactly 64 hexadecimal characters"
                );
    }

    @Test
    void shouldRejectNonHexadecimalSha256ThroughCanonicalConstructor() {
        String invalid =
                "z".repeat(64);

        assertThatThrownBy(
                () -> new ObservationFingerprint(
                        1,
                        ObservationFingerprint.SHA_256,
                        invalid
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "SHA-256 fingerprint must contain exactly 64 hexadecimal characters"
                );
    }

    @Test
    void directAndFactoryConstructionShouldProduceEqualSha256Fingerprints() {
        ObservationFingerprint throughFactory =
                ObservationFingerprint.sha256V1(
                        SHA_256
                );

        ObservationFingerprint directly =
                new ObservationFingerprint(
                        1,
                        ObservationFingerprint.SHA_256,
                        SHA_256.toUpperCase()
                );

        assertThat(directly)
                .isEqualTo(throughFactory);
    }

    @Test
    void genericFingerprintShouldRetainAlgorithmAndValue() {
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

        assertThat(fingerprint.isSha256())
                .isFalse();
    }

    @Test
    void genericFingerprintShouldPreserveCase() {
        ObservationFingerprint fingerprint =
                new ObservationFingerprint(
                        7,
                        "TEST",
                        "AbC123"
                );

        assertThat(fingerprint.value())
                .isEqualTo("AbC123");
    }
}