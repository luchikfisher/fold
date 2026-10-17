package com.fold.modules.claims.application.query;

import com.fold.modules.claims.api.query.ClaimQueries;
import com.fold.modules.claims.api.query.FindClaimsQuery;
import com.fold.modules.claims.api.query.GetClaimQuery;
import com.fold.modules.claims.api.view.ClaimSearchPage;
import com.fold.modules.claims.api.view.ClaimView;

import java.util.Objects;
import java.util.Optional;

/** Public read boundary, keeping aggregate detail and bounded discovery independent. */
public final class DefaultClaimQueries implements ClaimQueries {
    private final GetClaimHandler details;
    private final ClaimDiscoveryReader discovery;

    public DefaultClaimQueries(GetClaimHandler details, ClaimDiscoveryReader discovery) {
        this.details = Objects.requireNonNull(details);
        this.discovery = Objects.requireNonNull(discovery);
    }

    @Override
    public Optional<ClaimView> get(GetClaimQuery query) {
        return details.get(query);
    }

    @Override
    public ClaimSearchPage find(FindClaimsQuery query) {
        return discovery.find(Objects.requireNonNull(query));
    }
}
