package com.fold.modules.claims.api.query;

import com.fold.modules.claims.api.view.ClaimSearchPage;
import com.fold.modules.claims.api.view.ClaimView;

import java.util.Optional;

/**
 * Public read boundary for authoritative Claims.
 */
public interface ClaimQueries {

    /** Bounded live discovery; see FindClaimsQuery for continuation semantics. */
    ClaimSearchPage find(FindClaimsQuery query);

    Optional<ClaimView> get(
            GetClaimQuery query
    );
}
