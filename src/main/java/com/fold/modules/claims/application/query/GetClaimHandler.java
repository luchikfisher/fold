package com.fold.modules.claims.application.query;

import com.fold.modules.claims.api.query.ClaimQueries;
import com.fold.modules.claims.api.query.GetClaimQuery;
import com.fold.modules.claims.api.view.ClaimView;
import com.fold.modules.claims.domain.repository.ClaimRepository;

import java.util.Objects;
import java.util.Optional;

/**
 * Retrieves one authoritative Claim for public read access.
 */
public final class GetClaimHandler
        implements ClaimQueries {

    private final ClaimRepository repository;

    public GetClaimHandler(
            ClaimRepository repository
    ) {
        this.repository = Objects.requireNonNull(
                repository,
                "repository must not be null"
        );
    }

    @Override
    public Optional<ClaimView> get(
            GetClaimQuery query
    ) {
        Objects.requireNonNull(
                query,
                "query must not be null"
        );

        return repository
                .findById(query.claimId())
                .map(ClaimViewMapper::map);
    }
}