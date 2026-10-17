package com.fold.modules.claims.domain.policy;

import com.fold.modules.claims.domain.value.ClaimObject;
import com.fold.modules.claims.domain.value.ClaimPredicate;
import com.fold.modules.claims.domain.value.ClaimProposition;
import com.fold.modules.claims.domain.value.ClaimReference;
import com.fold.modules.claims.domain.value.ClaimSubject;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExactClaimPropositionEquivalencePolicyTest {

    private final ClaimPropositionEquivalencePolicy policy =
            new ExactClaimPropositionEquivalencePolicy();

    private static ClaimProposition proposition(
            String subject,
            String predicate,
            ClaimObject object
    ) {
        return new ClaimProposition(
                ClaimSubject.of(
                        "registry",
                        subject
                ),
                ClaimPredicate.of(predicate),
                object
        );
    }

    @Test
    void identicalPropositionsShouldBeEquivalent() {
        ClaimProposition first =
                proposition(
                        "company-1",
                        "country",
                        new ClaimObject.Text("Germany")
                );

        ClaimProposition second =
                proposition(
                        "company-1",
                        "country",
                        new ClaimObject.Text("Germany")
                );

        assertThat(
                policy.equivalent(first, second)
        ).isTrue();
    }

    @Test
    void differentSubjectsShouldNotBeEquivalent() {
        ClaimProposition first =
                proposition(
                        "company-1",
                        "country",
                        new ClaimObject.Text("Germany")
                );

        ClaimProposition second =
                proposition(
                        "company-2",
                        "country",
                        new ClaimObject.Text("Germany")
                );

        assertThat(
                policy.equivalent(first, second)
        ).isFalse();
    }

    @Test
    void differentPredicatesShouldNotBeEquivalent() {
        ClaimProposition first =
                proposition(
                        "company-1",
                        "country",
                        new ClaimObject.Text("Germany")
                );

        ClaimProposition second =
                proposition(
                        "company-1",
                        "registered-in",
                        new ClaimObject.Text("Germany")
                );

        assertThat(
                policy.equivalent(first, second)
        ).isFalse();
    }

    @Test
    void differentObjectsShouldNotBeEquivalent() {
        ClaimProposition first =
                proposition(
                        "company-1",
                        "country",
                        new ClaimObject.Text("Germany")
                );

        ClaimProposition second =
                proposition(
                        "company-1",
                        "country",
                        new ClaimObject.Text("France")
                );

        assertThat(
                policy.equivalent(first, second)
        ).isFalse();
    }

    @Test
    void typedValuesShouldRemainSemanticallyDistinct() {
        ClaimProposition textual =
                proposition(
                        "company-1",
                        "employee-count",
                        new ClaimObject.Text("1")
                );

        ClaimProposition numeric =
                proposition(
                        "company-1",
                        "employee-count",
                        ClaimObject.NumberValue.of("1")
                );

        assertThat(
                policy.equivalent(
                        textual,
                        numeric
                )
        ).isFalse();
    }

    @Test
    void referencesWithDifferentNamespacesShouldNotBeEquivalent() {
        ClaimProposition first =
                proposition(
                        "company-1",
                        "has-director",
                        new ClaimObject.Reference(
                                new ClaimReference(
                                        "registry-a",
                                        "person-1"
                                )
                        )
                );

        ClaimProposition second =
                proposition(
                        "company-1",
                        "has-director",
                        new ClaimObject.Reference(
                                new ClaimReference(
                                        "registry-b",
                                        "person-1"
                                )
                        )
                );

        assertThat(
                policy.equivalent(first, second)
        ).isFalse();
    }

    @Test
    void exactPolicyMustNotInferIdentityAcrossReferences() {
        ClaimProposition first =
                proposition(
                        "company-1",
                        "has-director",
                        new ClaimObject.Reference(
                                new ClaimReference(
                                        "registry-a",
                                        "123"
                                )
                        )
                );

        ClaimProposition second =
                proposition(
                        "company-1",
                        "has-director",
                        new ClaimObject.Reference(
                                new ClaimReference(
                                        "registry-b",
                                        "987"
                                )
                        )
                );

        /*
         * These references could later resolve to the same canonical Entity.
         * The exact Claims-domain policy deliberately does not know that.
         */
        assertThat(
                policy.equivalent(first, second)
        ).isFalse();
    }

    @Test
    void shouldRejectNullLeftProposition() {
        ClaimProposition proposition =
                proposition(
                        "company-1",
                        "country",
                        new ClaimObject.Text("Germany")
                );

        assertThatThrownBy(
                () -> policy.equivalent(
                        null,
                        proposition
                )
        )
                .isInstanceOf(
                        NullPointerException.class
                )
                .hasMessage(
                        "left must not be null"
                );
    }

    @Test
    void shouldRejectNullRightProposition() {
        ClaimProposition proposition =
                proposition(
                        "company-1",
                        "country",
                        new ClaimObject.Text("Germany")
                );

        assertThatThrownBy(
                () -> policy.equivalent(
                        proposition,
                        null
                )
        )
                .isInstanceOf(
                        NullPointerException.class
                )
                .hasMessage(
                        "right must not be null"
                );
    }
}