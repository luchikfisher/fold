package com.fold.modules.claims.application.feature.deriveclaims;

import com.fold.kernel.events.EventEnvelope;
import com.fold.modules.observations.api.event.ObservationAcceptedV1;

/**
 * Authoritative processing boundary for an accepted Observation.
 */
public interface ProcessAcceptedObservationUseCase {

    ProcessAcceptedObservationResult handle(
            EventEnvelope<ObservationAcceptedV1> event
    );
}