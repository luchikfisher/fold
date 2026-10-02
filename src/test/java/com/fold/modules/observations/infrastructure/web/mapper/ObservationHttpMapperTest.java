package com.fold.modules.observations.infrastructure.web.mapper;

import com.fold.modules.observations.domain.value.ObservationValue;
import com.fold.modules.observations.infrastructure.web.error.InvalidObservationRequestException;
import com.fold.modules.observations.infrastructure.web.request.ObservationValueRequest;
import com.fold.modules.observations.infrastructure.web.request.SubmitObservationRequest;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ObservationHttpMapperTest {

    private final ObjectMapper objectMapper =
            new ObjectMapper();

    private final ObservationHttpMapper mapper =
            new ObservationHttpMapper();

    @Test
    void shouldMapCompleteValidRequest() throws Exception {
        SubmitObservationRequest request =
                new SubmitObservationRequest(
                        "organization-registration",
                        new SubmitObservationRequest.Subject(
                                "organization",
                                "registry:123"
                        ),
                        List.of(
                                new SubmitObservationRequest.Field(
                                        "organization.name",
                                        new ObservationValueRequest(
                                                "text",
                                                objectMapper.readTree(
                                                        "\"Acme Ltd\""
                                                ),
                                                null,
                                                null
                                        )
                                )
                        ),
                        new SubmitObservationRequest.Origin(
                                "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
                                null,
                                "record-123"
                        ),
                        "2026-10-01T10:00:00Z"
                );

        var command =
                mapper.toCommand(request);

        assertThat(command.type().value())
                .isEqualTo(
                        "organization-registration"
                );

        assertThat(command.subject().externalKey())
                .isEqualTo("registry:123");

        assertThat(command.payload().size())
                .isEqualTo(1);
    }

    @Test
    void shouldMapEmptyTextValue() throws Exception {
        SubmitObservationRequest request =
                requestWithValue(
                        new ObservationValueRequest(
                                "text",
                                objectMapper.readTree("\"\""),
                                null,
                                null
                        )
                );

        var command =
                mapper.toCommand(request);

        ObservationValue.Text value =
                (ObservationValue.Text)
                        command.payload()
                                .asList()
                                .getFirst()
                                .value();

        assertThat(value.value()).isEmpty();
    }

    @Test
    void shouldRejectUnsupportedValueType() throws Exception {
        SubmitObservationRequest request =
                requestWithValue(
                        new ObservationValueRequest(
                                "object",
                                objectMapper.readTree("\"x\""),
                                null,
                                null
                        )
                );

        assertThatThrownBy(
                () -> mapper.toCommand(request)
        )
                .isInstanceOf(
                        InvalidObservationRequestException.class
                )
                .hasMessageContaining(
                        "unsupported observation value type"
                );
    }

    @Test
    void shouldRejectInvalidSourceUuid() {
        SubmitObservationRequest request =
                new SubmitObservationRequest(
                        "test",
                        new SubmitObservationRequest.Subject(
                                "subject",
                                "1"
                        ),
                        List.of(
                                new SubmitObservationRequest.Field(
                                        "name",
                                        new ObservationValueRequest(
                                                "text",
                                                objectMapper.valueToTree(
                                                        "value"
                                                ),
                                                null,
                                                null
                                        )
                                )
                        ),
                        new SubmitObservationRequest.Origin(
                                "not-a-uuid",
                                null,
                                null
                        ),
                        "2026-10-01T10:00:00Z"
                );

        assertThatThrownBy(
                () -> mapper.toCommand(request)
        )
                .isInstanceOf(
                        InvalidObservationRequestException.class
                );
    }

    @Test
    void shouldRejectEmptyPayload() {
        SubmitObservationRequest request =
                new SubmitObservationRequest(
                        "test",
                        new SubmitObservationRequest.Subject(
                                "subject",
                                "1"
                        ),
                        List.of(),
                        new SubmitObservationRequest.Origin(
                                "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
                                null,
                                null
                        ),
                        "2026-10-01T10:00:00Z"
                );

        assertThatThrownBy(
                () -> mapper.toCommand(request)
        )
                .isInstanceOf(
                        InvalidObservationRequestException.class
                )
                .hasMessageContaining(
                        "payload must not be empty"
                );
    }

    @Test
    void shouldRejectInvalidObservedAt() {
        SubmitObservationRequest request =
                new SubmitObservationRequest(
                        "test",
                        new SubmitObservationRequest.Subject(
                                "subject",
                                "1"
                        ),
                        List.of(
                                new SubmitObservationRequest.Field(
                                        "name",
                                        new ObservationValueRequest(
                                                "text",
                                                objectMapper.valueToTree(
                                                        "value"
                                                ),
                                                null,
                                                null
                                        )
                                )
                        ),
                        new SubmitObservationRequest.Origin(
                                "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
                                null,
                                null
                        ),
                        "yesterday-ish"
                );

        assertThatThrownBy(
                () -> mapper.toCommand(request)
        )
                .isInstanceOf(
                        InvalidObservationRequestException.class
                );
    }

    private SubmitObservationRequest requestWithValue(
            ObservationValueRequest value
    ) {
        return new SubmitObservationRequest(
                "test",
                new SubmitObservationRequest.Subject(
                        "subject",
                        "subject-1"
                ),
                List.of(
                        new SubmitObservationRequest.Field(
                                "field",
                                value
                        )
                ),
                new SubmitObservationRequest.Origin(
                        "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
                        null,
                        null
                ),
                "2026-10-01T10:00:00Z"
        );
    }
}