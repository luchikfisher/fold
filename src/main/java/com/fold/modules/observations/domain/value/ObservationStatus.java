package com.fold.modules.observations.domain.value;

/**
 * Represents the lifecycle state of an observation.
 *
 * <p>The lifecycle is deliberately small. An observation is received and is
 * then either accepted into the knowledge pipeline or rejected.</p>
 *
 * <p>Duplicate input is not an observation status. A duplicate submission
 * refers to an observation that already exists and therefore does not create
 * a second observation whose state is {@code DUPLICATE}.</p>
 */
public enum ObservationStatus {

    /**
     * The observation has been created but has not yet received its final
     * acceptance decision.
     */
    RECEIVED,

    /**
     * The observation satisfied the acceptance rules and may be consumed by
     * downstream knowledge processing.
     */
    ACCEPTED,

    /**
     * The observation was retained for traceability but was not admitted into
     * downstream knowledge processing.
     */
    REJECTED
}