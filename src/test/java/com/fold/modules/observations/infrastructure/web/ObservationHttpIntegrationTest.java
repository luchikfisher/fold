package com.fold.modules.observations.infrastructure.web;

import com.fold.testsupport.PostgresIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ObservationHttpIntegrationTest
        extends PostgresIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    JdbcTemplate jdbc;

    @Test
    void shouldSubmitAndRetrieveObservation()
            throws Exception {

        String sourceId =
                "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa";

        String request = """
                {
                  "type": "organization-registration",
                  "subject": {
                    "type": "organization",
                    "externalKey": "registry:company:123"
                  },
                  "payload": [
                    {
                      "name": "organization.name",
                      "value": {
                        "type": "text",
                        "value": "Acme Ltd"
                      }
                    },
                    {
                      "name": "organization.active",
                      "value": {
                        "type": "boolean",
                        "value": true
                      }
                    }
                  ],
                  "origin": {
                    "sourceId": "%s",
                    "externalRecordId": "record-123"
                  },
                  "observedAt": "2026-09-29T10:00:00Z"
                }
                """.formatted(sourceId);

        String response =
                mockMvc.perform(
                                post(
                                        "/api/v1/observations"
                                )
                                        .contentType(
                                                "application/json"
                                        )
                                        .content(request)
                        )
                        .andExpect(
                                status().isCreated()
                        )
                        .andExpect(
                                jsonPath("$.outcome")
                                        .value("ACCEPTED")
                        )
                        .andExpect(
                                jsonPath("$.duplicate")
                                        .value(false)
                        )
                        .andExpect(
                                jsonPath(
                                        "$.fingerprint.version"
                                ).value(1)
                        )
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        JsonNode json =
                new ObjectMapper()
                        .readTree(response);

        String observationId =
                json.get("observationId")
                        .asText();

        mockMvc.perform(
                        get(
                                "/api/v1/observations/{id}",
                                observationId
                        )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.id")
                                .value(observationId)
                )
                .andExpect(
                        jsonPath("$.status")
                                .value("ACCEPTED")
                )
                .andExpect(
                        jsonPath("$.type")
                                .value(
                                        "organization-registration"
                                )
                )
                .andExpect(
                        jsonPath(
                                "$.payload[0].name"
                        ).value(
                                "organization.name"
                        )
                );

        Integer observationCount =
                jdbc.queryForObject(
                        "SELECT count(*) FROM observations",
                        Integer.class
                );

        Integer outboxCount =
                jdbc.queryForObject(
                        """
                                SELECT count(*)
                                FROM integration_outbox
                                WHERE event_type =
                                    'observations.observation-accepted'
                                """,
                        Integer.class
                );

        assertThat(observationCount)
                .isEqualTo(1);

        assertThat(outboxCount)
                .isEqualTo(1);
    }

    @Test
    void repeatedSubmissionShouldReturnExistingObservation()
            throws Exception {

        String request = """
                {
                  "type": "organization-registration",
                  "subject": {
                    "type": "organization",
                    "externalKey": "registry:company:123"
                  },
                  "payload": [
                    {
                      "name": "organization.name",
                      "value": {
                        "type": "text",
                        "value": "Acme Ltd"
                      }
                    }
                  ],
                  "origin": {
                    "sourceId": "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
                    "externalRecordId": "record-duplicate-test"
                  },
                  "observedAt": "2026-09-29T10:00:00Z"
                }
                """;

        String first =
                mockMvc.perform(
                                post(
                                        "/api/v1/observations"
                                )
                                        .contentType(
                                                "application/json"
                                        )
                                        .content(request)
                        )
                        .andExpect(
                                status().isCreated()
                        )
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        String second =
                mockMvc.perform(
                                post(
                                        "/api/v1/observations"
                                )
                                        .contentType(
                                                "application/json"
                                        )
                                        .content(request)
                        )
                        .andExpect(
                                status().isOk()
                        )
                        .andExpect(
                                jsonPath("$.outcome")
                                        .value("DUPLICATE")
                        )
                        .andExpect(
                                jsonPath("$.duplicate")
                                        .value(true)
                        )
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        ObjectMapper mapper =
                new ObjectMapper();

        String firstId =
                mapper.readTree(first)
                        .get("observationId")
                        .asText();

        String secondId =
                mapper.readTree(second)
                        .get("observationId")
                        .asText();

        assertThat(secondId)
                .isEqualTo(firstId);
    }

    @Test
    void unknownObservationShouldReturn404()
            throws Exception {

        mockMvc.perform(
                        get(
                                "/api/v1/observations/{id}",
                                "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"
                        )
                )
                .andExpect(
                        status().isNotFound()
                );
    }

    @Test
    void malformedObservationShouldReturn400()
            throws Exception {

        mockMvc.perform(
                        post(
                                "/api/v1/observations"
                        )
                                .contentType(
                                        "application/json"
                                )
                                .content(
                                        """
                                                {
                                                  "type": "",
                                                  "payload": []
                                                }
                                                """
                                )
                )
                .andExpect(
                        status().isBadRequest()
                )
                .andExpect(
                        jsonPath("$.title")
                                .value(
                                        "Invalid observation request"
                                )
                );
    }
}