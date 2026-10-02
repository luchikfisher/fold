package com.fold.modules.claims.domain.value;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClaimPropositionTest {

    private static final ClaimSubject SUBJECT =
            ClaimSubject.of(
                    "company-registry",
                    "123"
            );

    private static final ClaimPredicate PREDICATE =
            ClaimPredicate.of(
                    "has-director"
            );

    private static final ClaimObject OBJECT =
            new ClaimObject.Reference(
                    new ClaimReference(
                            "person-registry",
                            "456"
                    )
            );

    @Test
    void shouldRepresentSubjectPredicateAndObject() {
        ClaimProposition proposition =
                new ClaimProposition(
                        SUBJECT,
                        PREDICATE,
                        OBJECT
                );

        assertThat(proposition.subject())
                .isEqualTo(SUBJECT);

        assertThat(proposition.predicate())
                .isEqualTo(PREDICATE);

        assertThat(proposition.object())
                .isEqualTo(OBJECT);
    }

    @Test
    void equalSemanticComponentsShouldProduceEqualProposition() {
        ClaimProposition first =
                new ClaimProposition(
                        SUBJECT,
                        PREDICATE,
                        OBJECT
                );

        ClaimProposition second =
                new ClaimProposition(
                        SUBJECT,
                        PREDICATE,
                        OBJECT
                );

        assertThat(first).isEqualTo(second);
    }

    @Test
    void shouldRejectNullSubject() {
        assertThatThrownBy(
                () -> new ClaimProposition(
                        null,
                        PREDICATE,
                        OBJECT
                )
        ).isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldRejectNullPredicate() {
        assertThatThrownBy(
                () -> new ClaimProposition(
                        SUBJECT,
                        null,
                        OBJECT
                )
        ).isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldRejectNullObject() {
        assertThatThrownBy(
                () -> new ClaimProposition(
                        SUBJECT,
                        PREDICATE,
                        null
                )
        ).isInstanceOf(NullPointerException.class);
    }
}