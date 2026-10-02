package com.fold.platform.events;

import com.fold.kernel.events.EventEnvelope;
import com.fold.kernel.events.EventMetadata;
import com.fold.kernel.ids.CorrelationId;
import com.fold.kernel.ids.EventId;
import com.fold.kernel.ids.ObservationId;
import com.fold.kernel.time.Timestamp;
import com.fold.modules.observations.api.event.ObservationAcceptedV1;
import com.fold.testsupport.PostgresIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class JdbcOutboxEventPublisherTest
        extends PostgresIntegrationTest {

    @Autowired
    JdbcOutboxEventPublisher publisher;

    @Autowired
    JdbcTemplate jdbc;

    @Test
    void shouldPersistCompleteEventEnvelope() {
        Timestamp occurredAt =
                Timestamp.parse(
                        "2026-10-01T10:00:00Z"
                );

        Timestamp emittedAt =
                Timestamp.parse(
                        "2026-10-01T10:00:01Z"
                );

        EventId eventId =
                EventId.random();

        CorrelationId correlationId =
                CorrelationId.random();

        EventId causationId =
                EventId.random();

        ObservationId observationId =
                ObservationId.random();

        ObservationAcceptedV1 event =
                new ObservationAcceptedV1(
                        observationId,
                        occurredAt
                );

        EventEnvelope<ObservationAcceptedV1> envelope =
                new EventEnvelope<>(
                        new EventMetadata(
                                eventId,
                                correlationId,
                                causationId,
                                emittedAt,
                                "observations"
                        ),
                        event
                );

        publisher.publish(envelope);

        Map<String, Object> row =
                jdbc.queryForMap(
                        """
                                SELECT
                                    event_id,
                                    correlation_id,
                                    causation_id,
                                    event_type,
                                    event_version,
                                    occurred_at,
                                    emitted_at,
                                    producer,
                                    payload::text AS payload,
                                    state,
                                    attempts
                                FROM integration_outbox
                                WHERE event_id = ?
                                """,
                        eventId.value()
                );

        assertThat(row.get("event_id"))
                .isEqualTo(eventId.value());

        assertThat(row.get("correlation_id"))
                .isEqualTo(correlationId.value());

        assertThat(row.get("causation_id"))
                .isEqualTo(causationId.value());

        assertThat(row.get("event_type"))
                .isEqualTo(
                        ObservationAcceptedV1.TYPE
                );

        assertThat(row.get("event_version"))
                .isEqualTo(1);

        assertThat(row.get("producer"))
                .isEqualTo("observations");

        assertThat(row.get("state"))
                .isEqualTo("PENDING");

        assertThat(row.get("attempts"))
                .isEqualTo(0);

        assertThat(row.get("occurred_at"))
                .isEqualTo(
                        java.sql.Timestamp.from(
                                occurredAt.value()
                        )
                );

        assertThat(row.get("emitted_at"))
                .isEqualTo(
                        java.sql.Timestamp.from(
                                emittedAt.value()
                        )
                );

        String payload =
                (String) row.get("payload");

        assertThat(payload)
                .contains(
                        observationId.asString()
                );
    }
}