package com.fold.modules.claims.application.feature.deriveclaims;

import com.fold.modules.claims.application.idempotency.ClaimProcessingKey;
import com.fold.modules.claims.domain.derivation.ClaimDerivationInput;
import com.fold.modules.claims.domain.derivation.ClaimDerivationRule;
import com.fold.modules.claims.domain.derivation.ClaimProposal;
import com.fold.modules.claims.domain.value.ClaimDerivation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Executes registered explicit semantic Claim derivation rules.
 *
 * <p>The authoritative processing workflow may first inspect applicable
 * derivations, perform an atomic idempotency acquisition, and only then execute
 * the corresponding derivation rule.</p>
 */
public final class ClaimDerivationEngine {

    private final List<ClaimDerivationRule> rules;
    private final Map<ClaimDerivation, ClaimDerivationRule> byDerivation;

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

        Map<ClaimDerivation, ClaimDerivationRule> indexed =
                new HashMap<>();

        for (ClaimDerivationRule rule : rules) {
            ClaimDerivation derivation =
                    Objects.requireNonNull(
                            rule.derivation(),
                            "rule derivation must not be null"
                    );

            ClaimDerivationRule previous =
                    indexed.putIfAbsent(
                            derivation,
                            rule
                    );

            if (previous != null) {
                throw new IllegalArgumentException(
                        "derivation rule identity must be unique: "
                                + derivation
                );
            }
        }

        this.rules = List.copyOf(rules);
        this.byDerivation = Map.copyOf(indexed);
    }

    /**
     * Returns every registered derivation that explicitly supports the input.
     *
     * <p>No Claim proposals are produced by this method.</p>
     */
    public List<ClaimDerivation> applicableDerivations(
            ClaimDerivationInput input
    ) {
        Objects.requireNonNull(
                input,
                "input must not be null"
        );

        List<ClaimDerivation> applicable =
                new ArrayList<>();

        for (ClaimDerivationRule rule : rules) {
            if (rule.supports(input)) {
                applicable.add(
                        rule.derivation()
                );
            }
        }

        return List.copyOf(applicable);
    }

    /**
     * Executes exactly one previously identified derivation.
     */
    public ClaimDerivationBatch derive(
            ClaimDerivationInput input,
            ClaimDerivation derivation
    ) {
        Objects.requireNonNull(
                input,
                "input must not be null"
        );

        Objects.requireNonNull(
                derivation,
                "derivation must not be null"
        );

        ClaimDerivationRule rule =
                byDerivation.get(derivation);

        if (rule == null) {
            throw new IllegalArgumentException(
                    "No Claim derivation rule is registered for "
                            + derivation
            );
        }

        if (!rule.supports(input)) {
            throw new IllegalArgumentException(
                    "Claim derivation no longer supports supplied input: "
                            + derivation
            );
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

        return new ClaimDerivationBatch(
                new ClaimProcessingKey(
                        input.observationId(),
                        derivation
                ),
                proposals
        );
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

        var requiredReferences =
                input.origin().toClaimOrigin().references();

        for (ClaimProposal proposal : proposals) {
            if (!proposal.origin()
                    .references().containsAll(requiredReferences)) {
                throw new IllegalArgumentException(
                        "Observation-derived Claim proposal must "
                                + "preserve all required upstream provenance"
                );
            }
        }
    }
}
