package com.fold.modules.claims.infrastructure.transaction;

import com.fold.kernel.events.EventEnvelope;
import com.fold.modules.claims.application.feature.deriveclaims.ProcessAcceptedObservationHandler;
import com.fold.modules.claims.application.feature.deriveclaims.ProcessAcceptedObservationResult;
import com.fold.modules.claims.application.feature.deriveclaims.ProcessAcceptedObservationUseCase;
import com.fold.modules.observations.api.event.ObservationAcceptedV1;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * Applies the production transaction boundary to authoritative Claim
 * processing.
 *
 * <p>Processing acquisition, Claim writes, processing completion, and outbox
 * publication therefore participate in one database transaction.</p>
 */
@Component
@Primary
public class TransactionalProcessAcceptedObservationUseCase
        implements ProcessAcceptedObservationUseCase {

    private final ProcessAcceptedObservationHandler delegate;

    public TransactionalProcessAcceptedObservationUseCase(
            ProcessAcceptedObservationHandler delegate
    ) {
        this.delegate = Objects.requireNonNull(
                delegate,
                "delegate must not be null"
        );
    }

    @Override
    @Transactional
    public ProcessAcceptedObservationResult handle(
            EventEnvelope<ObservationAcceptedV1> event
    ) {
        return delegate.handle(event);
    }
}