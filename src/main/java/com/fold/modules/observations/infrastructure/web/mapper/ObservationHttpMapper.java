package com.fold.modules.observations.infrastructure.web.mapper;

import com.fold.kernel.ids.EvidenceId;
import com.fold.kernel.ids.SourceId;
import com.fold.kernel.time.Timestamp;
import com.fold.modules.observations.api.view.ObservationValueView;
import com.fold.modules.observations.api.view.ObservationView;
import com.fold.modules.observations.application.feature.submitobservation.SubmitObservationCommand;
import com.fold.modules.observations.application.feature.submitobservation.SubmitObservationResult;
import com.fold.modules.observations.domain.model.ObservationField;
import com.fold.modules.observations.domain.model.ObservationOrigin;
import com.fold.modules.observations.domain.model.ObservationPayload;
import com.fold.modules.observations.domain.model.ObservationSubject;
import com.fold.modules.observations.domain.value.ObservationType;
import com.fold.modules.observations.domain.value.ObservationValue;
import com.fold.modules.observations.domain.value.ObservedAt;
import com.fold.modules.observations.infrastructure.web.error.InvalidObservationRequestException;
import com.fold.modules.observations.infrastructure.web.request.ObservationValueRequest;
import com.fold.modules.observations.infrastructure.web.request.SubmitObservationRequest;
import com.fold.modules.observations.infrastructure.web.response.ObservationResponse;
import com.fold.modules.observations.infrastructure.web.response.SubmitObservationResponse;
import tools.jackson.databind.JsonNode;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class ObservationHttpMapper {

    public SubmitObservationCommand toCommand(
            SubmitObservationRequest request
    ) {
        if (request == null) {
            throw invalid("request must not be null");
        }

        try {
            require(request.type(), "type");
            Objects.requireNonNull(
                    request.subject(),
                    "subject must not be null"
            );
            Objects.requireNonNull(
                    request.origin(),
                    "origin must not be null"
            );
            Objects.requireNonNull(
                    request.payload(),
                    "payload must not be null"
            );
            require(
                    request.observedAt(),
                    "observedAt"
            );

            if (request.payload().isEmpty()) {
                throw invalid(
                        "payload must not be empty"
                );
            }

            List<ObservationField> fields =
                    request.payload()
                            .stream()
                            .map(this::toField)
                            .toList();

            SubmitObservationRequest.Origin origin =
                    request.origin();

            SourceId sourceId =
                    new SourceId(
                            UUID.fromString(
                                    require(
                                            origin.sourceId(),
                                            "origin.sourceId"
                                    )
                            )
                    );

            EvidenceId evidenceId =
                    origin.evidenceId() == null
                            ? null
                            : new EvidenceId(
                            UUID.fromString(
                                    require(
                                            origin.evidenceId(),
                                            "origin.evidenceId"
                                    )
                            )
                    );

            return new SubmitObservationCommand(
                    ObservationType.of(
                            request.type()
                    ),
                    ObservationSubject.of(
                            require(
                                    request.subject().type(),
                                    "subject.type"
                            ),
                            require(
                                    request.subject().externalKey(),
                                    "subject.externalKey"
                            )
                    ),
                    new ObservationPayload(
                            new com.fold.kernel.collections.NonEmptyList<>(
                                    fields
                            )
                    ),
                    new ObservationOrigin(
                            sourceId,
                            evidenceId,
                            optionalNonBlank(
                                    origin.externalRecordId(),
                                    "origin.externalRecordId"
                            )
                    ),
                    ObservedAt.of(
                            Timestamp.parse(
                                    request.observedAt()
                            )
                    )
            );
        } catch (InvalidObservationRequestException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new InvalidObservationRequestException(
                    exception.getMessage() == null
                            ? "Invalid observation request"
                            : exception.getMessage(),
                    exception
            );
        }
    }

    public SubmitObservationResponse toResponse(
            SubmitObservationResult result
    ) {
        return switch (result) {
            case SubmitObservationResult.Accepted accepted -> new SubmitObservationResponse(
                    accepted.observationId()
                            .asString(),
                    "ACCEPTED",
                    false,
                    fingerprint(
                            accepted.fingerprint()
                    ),
                    null
            );

            case SubmitObservationResult.Rejected rejected -> new SubmitObservationResponse(
                    rejected.observationId()
                            .asString(),
                    "REJECTED",
                    false,
                    fingerprint(
                            rejected.fingerprint()
                    ),
                    new SubmitObservationResponse.Rejection(
                            rejected.rejection().code(),
                            rejected.rejection().message()
                    )
            );

            case SubmitObservationResult.Duplicate duplicate -> new SubmitObservationResponse(
                    duplicate.existingObservationId()
                            .asString(),
                    "DUPLICATE",
                    true,
                    fingerprint(
                            duplicate.fingerprint()
                    ),
                    null
            );
        };
    }

    public ObservationResponse toResponse(
            ObservationView view
    ) {
        return new ObservationResponse(
                view.id().asString(),
                view.type(),
                new ObservationResponse.Subject(
                        view.subject().type(),
                        view.subject().externalKey()
                ),
                view.payload()
                        .stream()
                        .map(field ->
                                new ObservationResponse.Field(
                                        field.name(),
                                        toResponseValue(
                                                field.value()
                                        )
                                )
                        )
                        .toList(),
                new ObservationResponse.Origin(
                        view.origin()
                                .sourceId()
                                .asString(),
                        view.origin()
                                .evidenceId() == null
                                ? null
                                : view.origin()
                                .evidenceId()
                                .asString(),
                        view.origin()
                                .externalRecordId()
                ),
                new ObservationResponse.Fingerprint(
                        view.fingerprint().version(),
                        view.fingerprint().algorithm(),
                        view.fingerprint().value()
                ),
                view.observedAt().toString(),
                view.arrivedAt().toString(),
                view.status(),
                view.decidedAt() == null
                        ? null
                        : view.decidedAt().toString(),
                view.rejection() == null
                        ? null
                        : new ObservationResponse.Rejection(
                        view.rejection().code(),
                        view.rejection().message()
                )
        );
    }

    private ObservationField toField(
            SubmitObservationRequest.Field field
    ) {
        if (field == null) {
            throw invalid(
                    "payload must not contain null fields"
            );
        }

        return ObservationField.of(
                require(
                        field.name(),
                        "payload field name"
                ),
                toValue(field.value())
        );
    }

    private ObservationValue toValue(
            ObservationValueRequest request
    ) {
        if (request == null) {
            throw invalid(
                    "observation value must not be null"
            );
        }

        String type =
                require(
                        request.type(),
                        "value.type"
                );

        return switch (type) {
            case "text" -> ObservationValue.Text.of(
                    requiredTextValue(
                            request.value(),
                            true
                    )
            );

            case "number" -> {
                JsonNode value =
                        requiredValueNode(
                                request.value()
                        );

                if (!value.isNumber()) {
                    throw invalid(
                            "number value must be numeric"
                    );
                }

                yield ObservationValue.Number.of(
                        value.decimalValue()
                );
            }

            case "boolean" -> {
                JsonNode value =
                        requiredValueNode(
                                request.value()
                        );

                if (!value.isBoolean()) {
                    throw invalid(
                            "boolean value must be true or false"
                    );
                }

                yield ObservationValue.BooleanValue.of(
                        value.booleanValue()
                );
            }

            case "timestamp" -> ObservationValue.TimestampValue.of(
                    Timestamp.parse(
                            requiredTextValue(
                                    request.value(),
                                    false
                            )
                    )
            );

            case "identifier" -> ObservationValue.Identifier.of(
                    require(
                            request.scheme(),
                            "identifier.scheme"
                    ),
                    requiredTextValue(
                            request.value(),
                            false
                    )
            );

            case "list" -> {
                if (request.values() == null
                        || request.values().isEmpty()) {
                    throw invalid(
                            "list values must not be empty"
                    );
                }

                List<ObservationValue> values =
                        request.values()
                                .stream()
                                .map(this::toValue)
                                .toList();

                yield new ObservationValue.ListValue(
                        new com.fold.kernel.collections.NonEmptyList<>(
                                values
                        )
                );
            }

            default -> throw invalid(
                    "unsupported observation value type: "
                            + type
            );
        };
    }

    private ObservationResponse.Value toResponseValue(
            ObservationValueView value
    ) {
        return switch (value) {
            case ObservationValueView.Text text -> new ObservationResponse.Value(
                    "text",
                    text.value(),
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );

            case ObservationValueView.Number number -> new ObservationResponse.Value(
                    "number",
                    null,
                    number.value(),
                    null,
                    null,
                    null,
                    null,
                    null
            );

            case ObservationValueView.BooleanValue booleanValue -> new ObservationResponse.Value(
                    "boolean",
                    null,
                    null,
                    booleanValue.value(),
                    null,
                    null,
                    null,
                    null
            );

            case ObservationValueView.TimestampValue timestamp -> new ObservationResponse.Value(
                    "timestamp",
                    null,
                    null,
                    null,
                    timestamp.value().toString(),
                    null,
                    null,
                    null
            );

            case ObservationValueView.Identifier identifier -> new ObservationResponse.Value(
                    "identifier",
                    null,
                    null,
                    null,
                    null,
                    identifier.scheme(),
                    identifier.value(),
                    null
            );

            case ObservationValueView.ListValue list -> new ObservationResponse.Value(
                    "list",
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    list.values()
                            .stream()
                            .map(this::toResponseValue)
                            .toList()
            );
        };
    }

    private static SubmitObservationResponse.Fingerprint fingerprint(
            com.fold.modules.observations.domain.value.ObservationFingerprint fingerprint
    ) {
        return new SubmitObservationResponse.Fingerprint(
                fingerprint.version(),
                fingerprint.algorithm(),
                fingerprint.value()
        );
    }

    private static JsonNode requiredValueNode(
            JsonNode value
    ) {
        if (value == null || value.isNull()) {
            throw invalid(
                    "value must not be null"
            );
        }

        return value;
    }

    private static String requiredTextValue(
            JsonNode node,
            boolean allowEmpty
    ) {
        node = requiredValueNode(node);

        if (!node.isTextual()) {
            throw invalid(
                    "value must be textual"
            );
        }

        String value =
                node.textValue();

        if (!allowEmpty && value.isBlank()) {
            throw invalid(
                    "value must not be blank"
            );
        }

        return value;
    }

    private static String require(
            String value,
            String name
    ) {
        if (value == null || value.isBlank()) {
            throw invalid(
                    name + " must not be blank"
            );
        }

        return value;
    }

    private static String optionalNonBlank(
            String value,
            String name
    ) {
        if (value == null) {
            return null;
        }

        if (value.isBlank()) {
            throw invalid(
                    name + " must not be blank when present"
            );
        }

        return value;
    }

    private static InvalidObservationRequestException invalid(
            String message
    ) {
        return new InvalidObservationRequestException(
                message
        );
    }
}