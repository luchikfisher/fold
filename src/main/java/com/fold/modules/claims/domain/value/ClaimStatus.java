package com.fold.modules.claims.domain.value;

/**
 * Represents authoritative lifecycle state of a Claim.
 *
 * <p>This enum contains lifecycle states only. Conditions such as disputed,
 * stale, weak, or corroborated are deliberately excluded because they are
 * assessments derived from other knowledge and are not intrinsic lifecycle
 * states of the assertion.</p>
 */
public enum ClaimStatus {

    /**
     * The claim is currently an active assertion in the knowledge model.
     */
    ACTIVE,

    /**
     * The claim has been replaced in an explicit revision or correction
     * lineage by another claim.
     *
     * <p>Supersession does not, by itself, mean that the earlier claim was
     * false.</p>
     */
    SUPERSEDED,

    /**
     * The assertion has been explicitly invalidated and should no longer
     * participate as an active claim.
     *
     * <p>Invalidation is distinct from staleness and from disagreement by
     * another claim.</p>
     */
    INVALIDATED
}