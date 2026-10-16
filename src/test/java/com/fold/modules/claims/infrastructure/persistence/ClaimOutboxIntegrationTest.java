package com.fold.modules.claims.infrastructure.persistence;

import com.fold.kernel.events.EventEnvelope;
import com.fold.kernel.events.EventMetadata;
import com.fold.kernel.ids.ClaimId;
import com.fold.kernel.time.Timestamp;
import com.fold.modules.claims.api.event.ClaimCreatedV1;
import com.fold.platform.events.JdbcOutboxEventPublisher;
import com.fold.platform.events.OutboxEventSerializer;
import com.fold.testsupport.PostgresIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import tools.jackson.databind.ObjectMapper;

import javax.sql.DataSource;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ClaimOutboxIntegrationTest
        extends PostgresIntegrationTest {

    @Autowired
    DataSource dataSource;

    @Autowired
    ObjectMapper objectMapper;

    private NamedParameterJdbcTemplate jdbc;

    @BeforeEach
    void setUp() {
        jdbc =
                new NamedParameterJdbcTemplate(
                        dataSource
                );

        jdbc.getJdbcOperations()
                .execute(
                        """
                        DELETE FROM integration_outbox;
                        """
                );
    }

    @Test
    void claimCreatedEnvelopeShouldPersistCompleteCausalMetadata() {
        Timestamp observationEventTime =
                Timestamp.parse(
                        "2026-10-03T10:00:00Z"
                );

        EventMetadata observationMetadata =
                EventMetadata.root(
                        observationEventTime,
                        "observations"
                );

        Timestamp emittedAt =
                Timestamp.parse(
                        "2026-10-03T10:00:01Z"
                );

        EventMetadata claimMetadata =
                EventMetadata.causedBy(
                        observationMetadata
                                .correlationId(),
                        observationMetadata
                                .eventId(),
                        emittedAt,
                        "claims"
                );

        ClaimCreatedV1 event =
                new ClaimCreatedV1(
                        ClaimId.random(),
                        Timestamp.parse(
                                "2026-10-03T10:00:00.500Z"
                        )
                );

        JdbcOutboxEventPublisher publisher =
                new JdbcOutboxEventPublisher(
                        jdbc,
                        new OutboxEventSerializer(
                                objectMapper
                        )
                );

        publisher.publish(
                new EventEnvelope<>(
                        claimMetadata,
                        event
                )
        );

        Map<String, Object> row =
                jdbc.getJdbcOperations()
                        .queryForMap(
                                """
                                SELECT
                                    event_id,
                                    correlation_id,
                                    causation_id,
                                    event_type,
                                    event_version,
                                    producer,
                                    state
                                FROM integration_outbox
                                """
                        );

        assertThat(row.get("event_id"))
                .isEqualTo(
                        claimMetadata
                                .eventId()
                                .value()
                );

        assertThat(row.get("correlation_id"))
                .isEqualTo(
                        observationMetadata
                                .correlationId()
                                .value()
                );

        assertThat(row.get("causation_id"))
                .isEqualTo(
                        observationMetadata
                                .eventId()
                                .value()
                );

        assertThat(row.get("event_type"))
                .isEqualTo(
                        ClaimCreatedV1.TYPE
                );

        assertThat(row.get("event_version"))
                .isEqualTo(1);

        assertThat(row.get("producer"))
                .isEqualTo("claims");

        assertThat(row.get("state"))
                .isEqualTo("PENDING");
    }
}