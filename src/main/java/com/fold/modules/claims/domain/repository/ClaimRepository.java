package com.fold.modules.claims.domain.repository;

import com.fold.kernel.ids.ClaimId;
import com.fold.modules.claims.domain.model.Claim;
import com.fold.modules.claims.domain.value.ClaimRevision;

import java.util.List;
import java.util.Optional;

/**
 * Domain persistence port for authoritative Claims.
 *
 * <p>This repository owns Claim persistence semantics only. It does not expose
 * generic CRUD operations and does not perform Observation, Evidence, Source,
 * or other bounded-context persistence.</p>
 */
public interface ClaimRepository {

    /**
     * Adds a newly created authoritative Claim.
     */
    void add(Claim claim);

    /**
     * Retrieves an authoritative Claim by identity.
     */
    Optional<Claim> findById(ClaimId id);

    /**
     * Locks existing Claims in a deterministic identity order until transaction
     * completion. All identities involved in an operation must be supplied in
     * one call, before loading state. Returns only identities actually locked;
     * callers must not load identities absent from this result, even if a
     * concurrent creation makes them visible later.
     * Requires an active transaction.
     */
    List<ClaimId> lockByIds(List<ClaimId> ids);

    /**
     * Persists exactly one new lifecycle revision and the resulting status.
     * Creation context and previous history are never rewritten. The caller
     * must hold the lifecycle locks and have loaded and transitioned the Claim
     * in the same transaction. A stale expected revision fails the write.
     */
    void appendLifecycleRevision(Claim claim, ClaimRevision expectedRevision);
}
