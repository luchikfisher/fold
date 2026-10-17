package com.fold.modules.claims.application.query;

import com.fold.modules.claims.api.query.FindClaimsQuery;
import com.fold.modules.claims.api.view.ClaimSearchPage;

/** Application read port; implementations must read each page in one coherent statement. */
public interface ClaimDiscoveryReader {
    ClaimSearchPage find(FindClaimsQuery query);
}
