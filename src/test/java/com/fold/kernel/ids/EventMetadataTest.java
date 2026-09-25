package com.fold.kernel.events;

import com.fold.kernel.ids.CorrelationId;
import com.fold.kernel.ids.EventId;
import com.fold.kernel.time.Timestamp;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EventMetadataTest {

    private static final Timestamp NOW =
            Timestamp.parse("2026-09-25T10:00:00Z");

    @Test
    void rootEventShouldStartNewCausalChain() {
        EventMetadata metadata =
                EventMetadata.root(NOW, "claims");

        assertThat(metadata.eventId()).isNotNull();

        assertThat(metadata.correlationId().value())
                .isEqualTo(metadata.eventId().value());

        assertThat(metadata.causation()).isEmpty();
        assertThat(metadata.isRoot()).isTrue();
        assertThat(metadata.emittedAt()).isEqualTo(NOW);
        assertThat(metadata.producer()).isEqualTo("claims");
    }

    @Test
    void causedEventShouldPreserveCorrelationId() {
        CorrelationId correlationId =
                CorrelationId.random();

        EventId cause =
                EventId.random();

        EventMetadata metadata =
                EventMetadata.causedBy(
                        correlationId,
                        cause,
                        NOW,
                        "beliefs"
                );

        assertThat(metadata.correlationId())
                .isEqualTo(correlationId);

        assertThat(metadata.causation())
                .contains(cause);

        assertThat(metadata.isRoot()).isFalse();

        assertThat(metadata.eventId())
                .isNotEqualTo(cause);
    }

    @Test
    void eachEmissionShouldReceiveUniqueEventId() {
        CorrelationId correlationId =
                CorrelationId.random();

        EventId cause =
                EventId.random();

        EventMetadata first =
                EventMetadata.causedBy(
                        correlationId,
                        cause,
                        NOW,
                        "beliefs"
                );

        EventMetadata second =
                EventMetadata.causedBy(
                        correlationId,
                        cause,
                        NOW,
                        "beliefs"
                );

        assertThat(first.eventId())
                .isNotEqualTo(second.eventId());
    }

    @Test
    void shouldRejectBlankProducer() {
        assertThatThrownBy(
                () -> EventMetadata.root(NOW, " ")
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("producer must not be blank");
    }

    @Test
    void shouldRejectNullProducer() {
        assertThatThrownBy(
                () -> EventMetadata.root(NOW, null)
        )
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void causedEventShouldRequireCause() {
        assertThatThrownBy(
                () -> EventMetadata.causedBy(
                        CorrelationId.random(),
                        null,
                        NOW,
                        "claims"
                )
        )
                .isInstanceOf(NullPointerException.class)
                .hasMessage("causationId must not be null");
    }
}