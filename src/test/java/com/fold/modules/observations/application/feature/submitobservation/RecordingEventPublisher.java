package com.fold.modules.observations.application.feature.submitobservation;

import com.fold.kernel.events.EventEnvelope;
import com.fold.kernel.events.EventPublisher;
import com.fold.kernel.events.IntegrationEvent;

import java.util.ArrayList;
import java.util.List;

final class RecordingEventPublisher
        implements EventPublisher {

    private final List<EventEnvelope<? extends IntegrationEvent>>
            events =
            new ArrayList<>();

    @Override
    public void publish(
            EventEnvelope<? extends IntegrationEvent> event
    ) {
        events.add(event);
    }

    List<EventEnvelope<? extends IntegrationEvent>> events() {
        return List.copyOf(events);
    }

    int size() {
        return events.size();
    }
}