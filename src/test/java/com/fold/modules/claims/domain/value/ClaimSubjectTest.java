package com.fold.modules.claims.domain.value;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClaimSubjectTest {

    @Test
    void shouldWrapClaimReference() {
        ClaimReference reference =
                new ClaimReference(
                        "company-registry",
                        "123"
                );

        ClaimSubject subject =
                new ClaimSubject(reference);

        assertThat(subject.reference())
                .isEqualTo(reference);
    }

    @Test
    void convenienceFactoryShouldCreateReference() {
        ClaimSubject subject =
                ClaimSubject.of(
                        "company-registry",
                        "123"
                );

        assertThat(subject.reference())
                .isEqualTo(
                        new ClaimReference(
                                "company-registry",
                                "123"
                        )
                );
    }

    @Test
    void shouldRejectNullReference() {
        assertThatThrownBy(
                () -> new ClaimSubject(null)
        )
                .isInstanceOf(
                        NullPointerException.class
                )
                .hasMessage(
                        "reference must not be null"
                );
    }
}