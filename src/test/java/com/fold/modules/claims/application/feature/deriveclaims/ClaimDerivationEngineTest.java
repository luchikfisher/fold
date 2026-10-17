package com.fold.modules.claims.application.feature.deriveclaims;

import com.fold.kernel.ids.ClaimId;
import com.fold.kernel.ids.EvidenceId;
import com.fold.kernel.ids.ObservationId;
import com.fold.kernel.ids.SourceId;
import com.fold.kernel.time.Timestamp;
import com.fold.modules.claims.domain.derivation.ClaimDerivationInput;
import com.fold.modules.claims.domain.derivation.ClaimDerivationOrigin;
import com.fold.modules.claims.domain.derivation.ClaimDerivationRule;
import com.fold.modules.claims.domain.derivation.ClaimDerivationSubject;
import com.fold.modules.claims.domain.derivation.ClaimProposal;
import com.fold.modules.claims.domain.value.ClaimDerivation;
import com.fold.modules.claims.domain.value.ClaimDerivationKind;
import com.fold.modules.claims.domain.value.ClaimDerivationMethod;
import com.fold.modules.claims.domain.value.ClaimOrigin;
import com.fold.modules.claims.domain.value.ClaimOriginReference;
import com.fold.testsupport.claims.ClaimTestFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClaimDerivationEngineTest {

    private static final ClaimDerivation FIRST =
            derivation(
                    "first-rule",
                    1
            );

    private static final ClaimDerivation SECOND =
            derivation(
                    "second-rule",
                    1
            );

    @Test
    void applicableDerivationsShouldNotExecuteDerive() {
        AtomicInteger executions =
                new AtomicInteger();

        ClaimDerivationRule rule =
                new TestRule(
                        FIRST,
                        true,
                        executions
                );

        ClaimDerivationEngine engine =
                new ClaimDerivationEngine(
                        List.of(rule)
                );

        assertThat(
                engine.applicableDerivations(
                        input()
                )
        ).containsExactly(FIRST);

        assertThat(executions.get())
                .isZero();
    }

    @Test
    void shouldReturnEveryApplicableRuleInRegistrationOrder() {
        ClaimDerivationEngine engine =
                new ClaimDerivationEngine(
                        List.of(
                                new TestRule(
                                        FIRST,
                                        true,
                                        new AtomicInteger()
                                ),
                                new TestRule(
                                        SECOND,
                                        true,
                                        new AtomicInteger()
                                )
                        )
                );

        assertThat(
                engine.applicableDerivations(
                        input()
                )
        ).containsExactly(
                FIRST,
                SECOND
        );
    }

    @Test
    void unsupportedRulesShouldBeExcluded() {
        ClaimDerivationEngine engine =
                new ClaimDerivationEngine(
                        List.of(
                                new TestRule(
                                        FIRST,
                                        false,
                                        new AtomicInteger()
                                )
                        )
                );

        assertThat(
                engine.applicableDerivations(
                        input()
                )
        ).isEmpty();
    }

    @Test
    void deriveShouldExecuteExactlySelectedRule() {
        AtomicInteger firstExecutions =
                new AtomicInteger();

        AtomicInteger secondExecutions =
                new AtomicInteger();

        ClaimDerivationInput input =
                input();

        ClaimDerivationEngine engine =
                new ClaimDerivationEngine(
                        List.of(
                                new TestRule(
                                        FIRST,
                                        true,
                                        firstExecutions
                                ),
                                new TestRule(
                                        SECOND,
                                        true,
                                        secondExecutions
                                )
                        )
                );

        ClaimDerivationBatch batch =
                engine.derive(
                        input,
                        FIRST
                );

        assertThat(batch.derivation())
                .isEqualTo(FIRST);

        assertThat(
                batch.processingKey()
                        .observationId()
        ).isEqualTo(
                input.observationId()
        );

        assertThat(firstExecutions.get())
                .isEqualTo(1);

        assertThat(secondExecutions.get())
                .isZero();
    }

    @Test
    void zeroProposalDerivationShouldStillProduceBatch() {
        ClaimDerivationEngine engine =
                new ClaimDerivationEngine(
                        List.of(
                                new TestRule(
                                        FIRST,
                                        true,
                                        new AtomicInteger()
                                )
                        )
                );

        ClaimDerivationBatch batch =
                engine.derive(
                        input(),
                        FIRST
                );

        assertThat(batch.isEmpty())
                .isTrue();

        assertThat(batch.size())
                .isZero();
    }

    @Test
    void duplicateDerivationIdentityShouldBeRejected() {
        assertThatThrownBy(
                () -> new ClaimDerivationEngine(
                        List.of(
                                new TestRule(
                                        FIRST,
                                        true,
                                        new AtomicInteger()
                                ),
                                new TestRule(
                                        FIRST,
                                        true,
                                        new AtomicInteger()
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
    void unknownDerivationShouldBeRejected() {
        ClaimDerivationEngine engine =
                new ClaimDerivationEngine(
                        List.of()
                );

        assertThatThrownBy(
                () -> engine.derive(
                        input(),
                        FIRST
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessageContaining(
                        "No Claim derivation rule"
                );
    }

    @Test
    void ruleThatNoLongerSupportsInputShouldNotRun() {
        ClaimDerivationEngine engine =
                new ClaimDerivationEngine(
                        List.of(
                                new TestRule(
                                        FIRST,
                                        false,
                                        new AtomicInteger()
                                )
                        )
                );

        assertThatThrownBy(
                () -> engine.derive(
                        input(),
                        FIRST
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessageContaining(
                        "no longer supports"
                );
    }

    @ParameterizedTest
    @ValueSource(classes = {ClaimOriginReference.Observation.class, ClaimOriginReference.Source.class,
            ClaimOriginReference.Evidence.class})
    void shouldRejectAnyProposalMissingRequiredProvenance(Class<? extends ClaimOriginReference> missingType) {
        ClaimDerivationInput input = input(EvidenceId.random());
        ClaimOrigin complete = input.origin().toClaimOrigin();
        var incomplete = new LinkedHashSet<>(complete.references());
        incomplete.removeIf(missingType::isInstance);
        // A different upstream identity of the same kind cannot replace the required one.
        incomplete.addAll(ClaimTestFixtures.origin().references());
        var proposals = List.of(ClaimProposal.of(ClaimTestFixtures.proposition(), complete),
                ClaimProposal.of(ClaimTestFixtures.proposition(), new ClaimOrigin(incomplete)));
        var engine = new ClaimDerivationEngine(List.of(new TestRule(FIRST, true, new AtomicInteger(), proposals)));

        assertThatThrownBy(() -> engine.derive(input, FIRST))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("required upstream provenance");
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void shouldPreserveCompleteProvenanceAndAllowAdditionalReferences(boolean hasEvidence) {
        ClaimDerivationInput input = input(hasEvidence ? EvidenceId.random() : null);
        ClaimOrigin complete = input.origin().toClaimOrigin();
        var extended = new LinkedHashSet<>(complete.references());
        extended.add(new ClaimOriginReference.Claim(ClaimId.random()));
        extended.add(new ClaimOriginReference.Source(SourceId.random()));
        var proposals = List.of(ClaimProposal.of(ClaimTestFixtures.proposition(), complete),
                ClaimProposal.of(ClaimTestFixtures.proposition(), new ClaimOrigin(extended)));
        var engine = new ClaimDerivationEngine(List.of(new TestRule(FIRST, true, new AtomicInteger(), proposals)));

        assertThat(engine.derive(input, FIRST).proposals()).containsExactlyElementsOf(proposals);
    }

    private static ClaimDerivationInput input() {
        return input(null);
    }

    private static ClaimDerivationInput input(EvidenceId evidenceId) {
        ObservationId observationId =
                ObservationId.random();

        return new ClaimDerivationInput(
                observationId,
                "test",
                new ClaimDerivationSubject(
                        "organization",
                        "123"
                ),
                List.of(),
                new ClaimDerivationOrigin(
                        observationId,
                        SourceId.random(),
                        evidenceId,
                        null
                ),
                Timestamp.parse(
                        "2026-10-03T09:00:00Z"
                )
        );
    }

    private static ClaimDerivation derivation(
            String method,
            int version
    ) {
        return new ClaimDerivation(
                ClaimDerivationKind.OBSERVED,
                new ClaimDerivationMethod(
                        method,
                        version
                )
        );
    }

    private static final class TestRule
            implements ClaimDerivationRule {

        private final ClaimDerivation derivation;
        private final boolean supports;
        private final AtomicInteger executions;
        private final List<ClaimProposal> proposals;

        private TestRule(
                ClaimDerivation derivation,
                boolean supports,
                AtomicInteger executions
        ) {
            this(derivation, supports, executions, List.of());
        }

        private TestRule(ClaimDerivation derivation, boolean supports, AtomicInteger executions,
                         List<ClaimProposal> proposals) {
            this.derivation = derivation;
            this.supports = supports;
            this.executions = executions;
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
            executions.incrementAndGet();
            return proposals;
        }
    }
}
