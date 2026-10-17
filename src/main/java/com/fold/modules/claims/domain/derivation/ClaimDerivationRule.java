package com.fold.modules.claims.domain.derivation;

import com.fold.modules.claims.domain.value.ClaimDerivation;

import java.util.List;

/**
 * Explicit semantic rule capable of producing Claims from one derivation
 * input.
 *
 * <p>A rule must represent known semantics. Implementations must not perform
 * generic "every field becomes a Claim" conversion.</p>
 *
 * <p>A supported input may legitimately produce zero Claims.</p>
 */
public interface ClaimDerivationRule {

    /**
     * Identifies the semantic derivation performed by this rule.
     *
     * <p>The method version is part of processing idempotency. A semantic
     * behavior change therefore requires a new derivation method version.</p>
     */
    ClaimDerivation derivation();

    /**
     * Determines whether this rule understands the supplied semantic input.
     */
    boolean supports(
            ClaimDerivationInput input
    );

    /**
     * Derives zero or more Claim proposals.
     *
     * <p>This method must be deterministic for the same input and rule
     * version. It must not persist Claims, generate ClaimIds, publish events,
     * call external services, or mutate upstream state.</p>
     */
    List<ClaimProposal> derive(
            ClaimDerivationInput input
    );
}