package com.fold.modules.observations.infrastructure.transaction;

import com.fold.modules.observations.application.feature.submitobservation.SubmitObservationCommand;
import com.fold.modules.observations.application.feature.submitobservation.SubmitObservationHandler;
import com.fold.modules.observations.application.feature.submitobservation.SubmitObservationResult;
import com.fold.modules.observations.application.feature.submitobservation.SubmitObservationUseCase;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * Applies the infrastructure transaction boundary to observation submission.
 *
 * <p>The application handler remains unaware of Spring. Observation insertion
 * and outbox publication participate in this same database transaction.</p>
 */
@Component
@Primary
public class TransactionalSubmitObservationUseCase
        implements SubmitObservationUseCase {

    private final SubmitObservationHandler delegate;

    public TransactionalSubmitObservationUseCase(
            SubmitObservationHandler delegate
    ) {
        this.delegate = Objects.requireNonNull(
                delegate,
                "delegate must not be null"
        );
    }

    @Override
    @Transactional
    public SubmitObservationResult handle(
            SubmitObservationCommand command
    ) {
        return delegate.handle(command);
    }
}