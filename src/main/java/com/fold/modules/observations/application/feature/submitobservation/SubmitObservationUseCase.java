package com.fold.modules.observations.application.feature.submitobservation;

/**
 * Application boundary for submitting one normalized observation to FOLD.
 *
 * <p>The interface exists so that infrastructure may apply transactional
 * behavior without introducing Spring or persistence concerns into the
 * application handler.</p>
 */
@FunctionalInterface
public interface SubmitObservationUseCase {

    /**
     * Executes one observation submission.
     *
     * @param command the normalized submission
     * @return the final submission outcome
     */
    SubmitObservationResult handle(
            SubmitObservationCommand command
    );
}