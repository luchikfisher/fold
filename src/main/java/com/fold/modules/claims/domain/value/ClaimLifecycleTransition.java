package com.fold.modules.claims.domain.value;

/**
 * Identifies an authoritative lifecycle transition recorded in Claim history.
 *
 * <p>A transition is an event in the history of a Claim, not the Claim's
 * current stable status.</p>
 *
 * <p>For example, {@link #REINSTATED} results in the stable lifecycle status
 * {@link ClaimStatus#ACTIVE}; there is deliberately no REINSTATED status.</p>
 */
public enum ClaimLifecycleTransition {

    /**
     * The Claim was first created and became active.
     */
    CREATED,

    /**
     * The Claim was explicitly replaced by another Claim.
     */
    SUPERSEDED,

    /**
     * The Claim was explicitly removed from active use.
     */
    INVALIDATED,

    /**
     * A previously invalidated Claim was explicitly returned to active use.
     */
    REINSTATED
}