package com.fold.modules.claims.application.derivation;

import com.fold.kernel.ids.ObservationId;
import com.fold.kernel.ids.SourceId;
import com.fold.kernel.time.Timestamp;
import com.fold.modules.claims.application.feature.deriveclaims.ClaimDerivationBatch;
import com.fold.modules.claims.application.feature.deriveclaims.ClaimDerivationEngine;
import com.fold.modules.claims.domain.derivation.ClaimDerivationInput;
import com.fold.modules.claims.domain.derivation.ClaimDerivationOrigin;
import com.fold.modules.claims.domain.derivation.ClaimDerivationRule;
import com.fold.modules.claims.domain.derivation.ClaimDerivationSubject;
import com.fold.modules.claims.domain.derivation.ClaimProposal;
import com.fold.modules.claims.domain.value.ClaimDerivation;
import com.fold.modules.claims.domain.value.ClaimDerivationKind;
import com.fold.modules.claims.domain.value.ClaimDerivationMethod;
import com.fold.modules.claims.domain.value.ClaimObject;
import com.fold.modules.claims.domain.value.ClaimOrigin;
import com.fold.modules.claims.domain.value.ClaimPredicate;
import com.fold.modules.claims.domain.value.ClaimProposition;
import com.fold.modules.claims.domain.value.ClaimSubject;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClaimDerivationEngineTest {

    private static final ClaimDerivation DERIVATION =
            new ClaimDerivation(
                    ClaimDerivationKind.OBSERVED,
                    ClaimDerivationMethod.versionOne(
                            "test-rule"
                    )
            );

    private static ClaimDerivationInput input() {
        ObservationId observationId =
                ObservationId.random();

        return new ClaimDerivationInput(
                observationId,
                "test-type",
                new ClaimDerivationSubject(
                        "organization",
                        "123"
                ),
                List.of(),
                new ClaimDerivationOrigin(
                        observationId,
                        SourceId.random(),
                        null,
                        null
                ),
                Timestamp.parse(
                        "2026-10-03T10:00:00Z"
                )
        );
    }

    @Test
    void noRulesShouldProduceNoBatches() {
        ClaimDerivationEngine engine =
                new ClaimDerivationEngine(
                        List.of()
                );

        assertThat(engine.derive(input()))
                .isEmpty();
    }

    @Test
    void unsupportedRuleShouldBeIgnored() {
        ClaimDerivationEngine engine =
                new ClaimDerivationEngine(
                        List.of(
                                new TestRule(
                                        false,
                                        List.of()
                                )
                        )
                );

        assertThat(engine.derive(input()))
                .isEmpty();
    }

    @Test
    void supportedZeroOutputRuleShouldStillProduceBatch() {
        ClaimDerivationInput input =
                input();

        ClaimDerivationEngine engine =
                new ClaimDerivationEngine(
                        List.of(
                                new TestRule(
                                        true,
                                        List.of()
                                )
                        )
                );

        List<ClaimDerivationBatch> batches =
                engine.derive(input);

        assertThat(batches)
                .hasSize(1);

        assertThat(batches.getFirst().isEmpty())
                .isTrue();

        assertThat(
                batches.getFirst()
                        .processingKey()
                        .observationId()
        ).isEqualTo(input.observationId());

        assertThat(
                batches.getFirst()
                        .processingKey()
                        .derivation()
        ).isEqualTo(DERIVATION);
    }

    @Test
    void supportedRuleShouldProduceItsClaims() {
        ClaimDerivationInput input =
                input();

        ClaimProposal proposal =
                proposalWithOrigin(
                        input.origin().toClaimOrigin()
                );

        ClaimDerivationEngine engine =
                new ClaimDerivationEngine(
                        List.of(
                                new TestRule(
                                        true,
                                        List.of(proposal)
                                )
                        )
                );

        assertThat(
                engine.derive(input)
                        .getFirst()
                        .proposals()
        ).containsExactly(proposal);
    }

    @Test
    void multipleApplicableRulesShouldEachProduceBatch() {
        ClaimDerivation ruleTwoDerivation =
                new ClaimDerivation(
                        ClaimDerivationKind.DERIVED,
                        ClaimDerivationMethod.versionOne(
                                "second-rule"
                        )
                );

        ClaimDerivationInput input =
                input();

        ClaimDerivationEngine engine =
                new ClaimDerivationEngine(
                        List.of(
                                new TestRule(
                                        DERIVATION,
                                        true,
                                        List.of()
                                ),
                                new TestRule(
                                        ruleTwoDerivation,
                                        true,
                                        List.of()
                                )
                        )
                );

        assertThat(engine.derive(input))
                .hasSize(2);
    }

    @Test
    void duplicateRuleIdentitiesShouldBeRejected() {
        assertThatThrownBy(
                () -> new ClaimDerivationEngine(
                        List.of(
                                new TestRule(
                                        DERIVATION,
                                        true,
                                        List.of()
                                ),
                                new TestRule(
                                        DERIVATION,
                                        true,
                                        List.of()
                                )
                        )
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessageContaining(
                        "must be unique"
                );
    }

    @Test
    void proposalMustReferenceOriginatingObservation() {
        ClaimDerivationInput input =
                input();

        ClaimProposal proposal =
                proposalWithOrigin(
                        ClaimOrigin.none()
                );

        ClaimDerivationEngine engine =
                new ClaimDerivationEngine(
                        List.of(
                                new TestRule(
                                        true,
                                        List.of(proposal)
                                )
                        )
                );

        assertThatThrownBy(
                () -> engine.derive(input)
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessageContaining(
                        "originating Observation"
                );
    }

    @Test
    void ruleMustNotReturnNullCollection() {
        ClaimDerivationEngine engine =
                new ClaimDerivationEngine(
                        List.of(
                                new ClaimDerivationRule() {
                                    @Override
                                    public ClaimDerivation derivation() {
                                        return DERIVATION;
                                    }

                                    @Override
                                    public boolean supports(
                                            ClaimDerivationInput input
                                    ) {
                                        return true;
                                    }

                                    @Override
                                    public List<ClaimProposal> derive(
                                            ClaimDerivationInput input
                                    ) {
                                        return null;
                                    }
                                }
                        )
                );

        assertThatThrownBy(
                () -> engine.derive(input())
        )
                .isInstanceOf(
                        NullPointerException.class
                )
                .hasMessageContaining(
                        "must not return null"
                );
    }

    private static ClaimProposal proposalWithOrigin(
            ClaimOrigin origin
    ) {
        return ClaimProposal.of(
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
                ),
                origin
        );
    }

    private static final class TestRule
            implements ClaimDerivationRule {

        private final ClaimDerivation derivation;
        private final boolean supports;
        private final List<ClaimProposal> proposals;

        private TestRule(
                boolean supports,
                List<ClaimProposal> proposals
        ) {
            this(
                    DERIVATION,
                    supports,
                    proposals
            );
        }

        private TestRule(
                ClaimDerivation derivation,
                boolean supports,
                List<ClaimProposal> proposals
        ) {
            this.derivation = derivation;
            this.supports = supports;
            this.proposals = proposals;
        }

        @Override
        public ClaimDerivation derivation() {
            return derivation;
        }

        @Override
        public boolean supports(
                ClaimDerivationInput input
        ) {
            return supports;
        }

        @Override
        public List<ClaimProposal> derive(
                ClaimDerivationInput input
        ) {
            return proposals;
        }
    }
}