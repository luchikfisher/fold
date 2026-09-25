package com.fold.kernel.events;

import com.fold.kernel.time.Timestamp;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EventEnvelopeTest {

    private static final Timestamp OCCURRED_AT =
            Timestamp.parse("2026-09-25T09:59:00Z");

    private static final Timestamp EMITTED_AT =
            Timestamp.parse("2026-09-25T10:00:00Z");

    @Test
    void shouldKeepPayloadAndMetadataTogether() {
        TestIntegrationEvent event =
                new TestIntegrationEvent(OCCURRED_AT);

        EventMetadata metadata =
                EventMetadata.root(
                        EMITTED_AT,
                        "test"
                );

        EventEnvelope<TestIntegrationEvent> envelope =
                new EventEnvelope<>(metadata, event);

        assertThat(envelope.payload()).isEqualTo(event);
        assertThat(envelope.metadata()).isEqualTo(metadata);
    }

    @Test
    void shouldExposePayloadContractMetadata() {
        EventEnvelope<TestIntegrationEvent> envelope =
                new EventEnvelope<>(
                        EventMetadata.root(
                                EMITTED_AT,
                                "test"
                        ),
                        new TestIntegrationEvent(OCCURRED_AT)
                );

        assertThat(envelope.eventType())
                .isEqualTo("test-event");

        assertThat(envelope.version())
                .isEqualTo(EventVersion.initial());
    }

    @Test
    void shouldRejectNullMetadata() {
        assertThatThrownBy(
                () -> new EventEnvelope<>(
                        null,
                        new TestIntegrationEvent(OCCURRED_AT)
                )
        )
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldRejectNullPayload() {
        assertThatThrownBy(
                () -> new EventEnvelope<TestIntegrationEvent>(
                        EventMetadata.root(
                                EMITTED_AT,
                                "test"
                        ),
                        null
                )
        )
                .isInstanceOf(NullPointerException.class);
    }

    private record TestIntegrationEvent(
            Timestamp occurredAt
    ) implements IntegrationEvent {

        @Override
        public String eventType() {
            return "test-event";
        }

        @Override
        public EventVersion version() {
            return EventVersion.initial();
        }
    }
}