package com.fold.modules.claims.application.feature.deriveclaims;

import com.fold.modules.claims.application.idempotency.ClaimProcessingKey;
import com.fold.modules.claims.domain.derivation.ClaimDerivationInput;
import com.fold.modules.claims.domain.derivation.ClaimDerivationRule;
import com.fold.modules.claims.domain.derivation.ClaimProposal;
import com.fold.modules.claims.domain.value.ClaimDerivation;
import com.fold.modules.claims.domain.value.ClaimOriginReference;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Applies registered semantic Claim derivation rules to one normalized
 * derivation input.
 *
 * <p>The engine performs orchestration and structural safety checks only. It
 * does not invent Claim semantics. All semantic production belongs to explicit
 * {@link ClaimDerivationRule} implementations.</p>
 */
public final class ClaimDerivationEngine {

    private final List<ClaimDerivationRule> rules;

    public ClaimDerivationEngine(
            List<ClaimDerivationRule> rules
    ) {
        Objects.requireNonNull(
                rules,
                "rules must not be null"
        );

        if (rules.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException(
                    "rules must not contain null"
            );
        }

        validateUniqueDerivations(rules);

        this.rules = List.copyOf(rules);
    }

    /**
     * Executes every rule that explicitly supports the supplied input.
     *
     * <p>A supported rule always produces a batch, including when the rule
     * produces zero Claim proposals.</p>
     */
    public List<ClaimDerivationBatch> derive(
            ClaimDerivationInput input
    ) {
        Objects.requireNonNull(
                input,
                "input must not be null"
        );

        List<ClaimDerivationBatch> batches =
                new ArrayList<>();

        for (ClaimDerivationRule rule : rules) {
            if (!rule.supports(input)) {
                continue;
            }

            List<ClaimProposal> proposals =
                    Objects.requireNonNull(
                            rule.derive(input),
                            "derivation rule must not return null"
                    );

            validateProposals(
                    input,
                    proposals
            );

            ClaimProcessingKey processingKey =
                    new ClaimProcessingKey(
                            input.observationId(),
                            rule.derivation()
                    );

            batches.add(
                    new ClaimDerivationBatch(
                            processingKey,
                            proposals
                    )
            );
        }

        return List.copyOf(batches);
    }

    private static void validateUniqueDerivations(
            List<ClaimDerivationRule> rules
    ) {
        Set<ClaimDerivation> derivations =
                new HashSet<>();

        for (ClaimDerivationRule rule : rules) {
            ClaimDerivation derivation =
                    Objects.requireNonNull(
                            rule.derivation(),
                            "rule derivation must not be null"
                    );

            if (!derivations.add(derivation)) {
                throw new IllegalArgumentException(
                        "derivation rule identity must be unique: "
                                + derivation
                );
            }
        }
    }

    private static void validateProposals(
            ClaimDerivationInput input,
            List<ClaimProposal> proposals
    ) {
        if (proposals.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException(
                    "derivation rule must not return null proposals"
            );
        }

        ClaimOriginReference.Observation requiredOrigin =
                new ClaimOriginReference.Observation(
                        input.observationId()
                );

        for (ClaimProposal proposal : proposals) {
            if (!proposal.origin().contains(requiredOrigin)) {
                throw new IllegalArgumentException(
                        "Observation-derived Claim proposal must "
                                + "reference its originating Observation"
                );
            }
        }
    }
}