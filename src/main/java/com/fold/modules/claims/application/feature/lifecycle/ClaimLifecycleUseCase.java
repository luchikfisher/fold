package com.fold.modules.claims.application.feature.lifecycle;

/**
 * Authoritative, transactional boundary for explicit Claim lifecycle decisions.
 * Commands are internal application contracts; public consumers use Claims
 * integration events and queries. See package documentation for retry semantics.
 */
public interface ClaimLifecycleUseCase {

    ClaimLifecycleResult handle(InvalidateClaimCommand command);

    ClaimLifecycleResult handle(ReinstateClaimCommand command);

    ClaimLifecycleResult handle(SupersedeClaimCommand command);
}
