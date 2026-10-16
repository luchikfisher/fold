package com.fold.modules.claims.api.query;

import com.fold.modules.claims.api.view.ClaimView;

import java.util.Optional;

/**
 * Public read boundary for authoritative Claims.
 */
public interface ClaimQueries {

    Optional<ClaimView> get(
            GetClaimQuery query
    );
}