package com.fold.platform.events;

import com.fold.kernel.events.EventEnvelope;
import com.fold.kernel.events.EventPublisher;
import com.fold.kernel.events.IntegrationEvent;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.Objects;

@Component
public final class JdbcOutboxEventPublisher
        implements EventPublisher {

    private final NamedParameterJdbcTemplate jdbc;
    private final OutboxEventSerializer serializer;

    public JdbcOutboxEventPublisher(
            NamedParameterJdbcTemplate jdbc,
            OutboxEventSerializer serializer
    ) {
        this.jdbc = Objects.requireNonNull(
                jdbc,
                "jdbc must not be null"
        );
        this.serializer = Objects.requireNonNull(
                serializer,
                "serializer must not be null"
        );
    }

    /**
     * Durably enqueues an integration event in the transactional outbox.
     *
     * <p>This operation does not send to a message broker directly. When called
     * inside the same database transaction as a domain write, either both the
     * domain state and outbox event commit or neither does.</p>
     *
     * @param envelope the event to enqueue
     */
    @Override
    public void publish(
            EventEnvelope<? extends IntegrationEvent> envelope
    ) {
        Objects.requireNonNull(
                envelope,
                "envelope must not be null"
        );

        String sql = """
                INSERT INTO integration_outbox
                (
                    event_id,
                    correlation_id,
                    causation_id,
                    event_type,
                    event_version,
                    occurred_at,
                    emitted_at,
                    producer,
                    payload
                )
                VALUES
                (
                    :eventId,
                    :correlationId,
                    :causationId,
                    :eventType,
                    :eventVersion,
                    :occurredAt,
                    :emittedAt,
                    :producer,
                    CAST(:payload AS jsonb)
                )
                """;

        jdbc.update(
                sql,
                new MapSqlParameterSource()
                        .addValue(
                                "eventId",
                                envelope.metadata()
                                        .eventId()
                                        .value()
                        )
                        .addValue(
                                "correlationId",
                                envelope.metadata()
                                        .correlationId()
                                        .value()
                        )
                        .addValue(
                                "causationId",
                                envelope.metadata()
                                        .causation()
                                        .map(eventId ->
                                                eventId.value()
                                        )
                                        .orElse(null)
                        )
                        .addValue(
                                "eventType",
                                envelope.eventType()
                        )
                        .addValue(
                                "eventVersion",
                                envelope.version().value()
                        )
                        .addValue(
                                "occurredAt",
                                Timestamp.from(
                                        envelope.payload()
                                                .occurredAt()
                                                .value()
                                )
                        )
                        .addValue(
                                "emittedAt",
                                Timestamp.from(
                                        envelope.metadata()
                                                .emittedAt()
                                                .value()
                                )
                        )
                        .addValue(
                                "producer",
                                envelope.metadata()
                                        .producer()
                        )
                        .addValue(
                                "payload",
                                serializer.serialize(
                                        envelope.payload()
                                )
                        )
        );
    }
}