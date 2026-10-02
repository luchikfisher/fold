package com.fold.modules.claims.domain.value;

/**
 * Describes the broad epistemic manner in which a claim entered FOLD.
 *
 * <p>This is not a lifecycle status and does not indicate whether the claim is
 * currently believed to be true.</p>
 */
public enum ClaimDerivationKind {

    /**
     * The claim represents an assertion explicitly present in observed input.
     */
    OBSERVED,

    /**
     * The claim was deterministically or rulefully derived from other known
     * information.
     */
    DERIVED,

    /**
     * The claim was produced through an inferential process whose conclusion
     * goes beyond direct deterministic extraction.
     */
    INFERRED,

    /**
     * The assertion was entered directly by a human analyst.
     */
    ANALYST_ENTERED
}