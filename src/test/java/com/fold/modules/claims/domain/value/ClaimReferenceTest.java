package com.fold.modules.claims.domain.value;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClaimReferenceTest {

    @Test
    void shouldPreserveNamespaceAndValueExactly() {
        ClaimReference reference =
                new ClaimReference(
                        "Company-Registry",
                        " ABC-123 "
                );

        assertThat(reference.namespace())
                .isEqualTo("Company-Registry");

        assertThat(reference.value())
                .isEqualTo(" ABC-123 ");
    }

    @Test
    void shouldUseBothNamespaceAndValueForEquality() {
        ClaimReference first =
                new ClaimReference(
                        "registry-a",
                        "123"
                );

        ClaimReference same =
                new ClaimReference(
                        "registry-a",
                        "123"
                );

        ClaimReference differentNamespace =
                new ClaimReference(
                        "registry-b",
                        "123"
                );

        assertThat(first).isEqualTo(same);
        assertThat(first)
                .isNotEqualTo(differentNamespace);
    }

    @Test
    void shouldRejectNullNamespace() {
        assertThatThrownBy(
                () -> new ClaimReference(
                        null,
                        "123"
                )
        )
                .isInstanceOf(
                        NullPointerException.class
                )
                .hasMessage(
                        "namespace must not be null"
                );
    }

    @Test
    void shouldRejectBlankNamespace() {
        assertThatThrownBy(
                () -> new ClaimReference(
                        " ",
                        "123"
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "namespace must not be blank"
                );
    }

    @Test
    void shouldRejectBlankValue() {
        assertThatThrownBy(
                () -> new ClaimReference(
                        "registry",
                        " "
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "value must not be blank"
                );
    }
}