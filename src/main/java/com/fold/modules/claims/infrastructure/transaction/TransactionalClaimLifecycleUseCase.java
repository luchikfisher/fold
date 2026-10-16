package com.fold.modules.claims.infrastructure.transaction;

import com.fold.modules.claims.application.feature.lifecycle.ClaimLifecycleHandler;
import com.fold.modules.claims.application.feature.lifecycle.ClaimLifecycleResult;
import com.fold.modules.claims.application.feature.lifecycle.ClaimLifecycleUseCase;
import com.fold.modules.claims.application.feature.lifecycle.InvalidateClaimCommand;
import com.fold.modules.claims.application.feature.lifecycle.ReinstateClaimCommand;
import com.fold.modules.claims.application.feature.lifecycle.SupersedeClaimCommand;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/** Keeps lifecycle locks, status, revision history, and outbox publication atomic. */
@Component
@Primary
public class TransactionalClaimLifecycleUseCase implements ClaimLifecycleUseCase {

    private final ClaimLifecycleHandler delegate;

    public TransactionalClaimLifecycleUseCase(ClaimLifecycleHandler delegate) {
        this.delegate = Objects.requireNonNull(delegate);
    }

    @Override
    @Transactional
    public ClaimLifecycleResult handle(InvalidateClaimCommand command) {
        return delegate.handle(command);
    }

    @Override
    @Transactional
    public ClaimLifecycleResult handle(ReinstateClaimCommand command) {
        return delegate.handle(command);
    }

    @Override
    @Transactional
    public ClaimLifecycleResult handle(SupersedeClaimCommand command) {
        return delegate.handle(command);
    }
}
