package com.fold.modules.claims.domain.repository;

import com.fold.kernel.ids.ClaimId;
import com.fold.modules.claims.domain.model.Claim;

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
}