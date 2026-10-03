package com.fold.modules.claims.domain.model;

import com.fold.kernel.ids.ClaimId;
import com.fold.kernel.time.Timestamp;
import com.fold.modules.claims.domain.policy.ExactClaimPropositionEquivalencePolicy;
import com.fold.modules.claims.domain.value.ClaimDerivation;
import com.fold.modules.claims.domain.value.ClaimDerivationKind;
import com.fold.modules.claims.domain.value.ClaimDerivationMethod;
import com.fold.modules.claims.domain.value.ClaimObject;
import com.fold.modules.claims.domain.value.ClaimOrigin;
import com.fold.modules.claims.domain.value.ClaimPredicate;
import com.fold.modules.claims.domain.value.ClaimProposition;
import com.fold.modules.claims.domain.value.ClaimSubject;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ClaimIdentitySemanticsTest {

    private static final Timestamp CREATED_AT =
            Timestamp.parse(
                    "2026-10-02T10:00:00Z"
            );

    private static final ClaimProposition PROPOSITION =
            new ClaimProposition(
                    ClaimSubject.of(
                            "registry",
                            "company-123"
                    ),
                    ClaimPredicate.of(
                            "country"
                    ),
                    new ClaimObject.Text(
                            "Germany"
                    )
            );

    private static final ClaimDerivation DERIVATION =
            new ClaimDerivation(
                    ClaimDerivationKind.OBSERVED,
                    ClaimDerivationMethod.versionOne(
                            "company-country-extractor"
                    )
            );

    @Test
    void equivalentPropositionsDoNotMakeClaimsIdentical() {
        Claim first =
                Claim.create(
                        ClaimId.random(),
                        PROPOSITION,
                        ClaimOrigin.none(),
                        DERIVATION,
                        null,
                        null,
                        null,
                        CREATED_AT
                ).claim();

        Claim second =
                Claim.create(
                        ClaimId.random(),
                        PROPOSITION,
                        ClaimOrigin.none(),
                        DERIVATION,
                        null,
                        null,
                        null,
                        CREATED_AT
                ).claim();

        ExactClaimPropositionEquivalencePolicy policy =
                new ExactClaimPropositionEquivalencePolicy();

        assertThat(
                policy.equivalent(
                        first.proposition(),
                        second.proposition()
                )
        ).isTrue();

        assertThat(first)
                .isNotEqualTo(second);

        assertThat(first.id())
                .isNotEqualTo(second.id());
    }
}