package com.fold.platform.events;

import com.fold.kernel.events.IntegrationEvent;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.util.Objects;

public final class OutboxEventSerializer {

    private final ObjectMapper objectMapper;

    public OutboxEventSerializer(
            ObjectMapper objectMapper
    ) {
        this.objectMapper = Objects.requireNonNull(
                objectMapper,
                "objectMapper must not be null"
        );
    }

    public String serialize(
            IntegrationEvent event
    ) {
        Objects.requireNonNull(
                event,
                "event must not be null"
        );

        try {
            return objectMapper.writeValueAsString(
                    event
            );
        } catch (JacksonException exception) {
            throw new IllegalStateException(
                    "Failed to serialize integration event "
                            + event.eventType(),
                    exception
            );
        }
    }
}