package com.fold.modules.observations.infrastructure.persistence.mapper;

import com.fold.kernel.time.Timestamp;
import com.fold.modules.observations.domain.model.ObservationField;
import com.fold.modules.observations.domain.model.ObservationPayload;
import com.fold.modules.observations.domain.value.ObservationValue;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Converts observation payloads to and from the persistence JSON format.
 *
 * <p>The JSON representation is an infrastructure concern. The observations
 * domain remains entirely independent of Jackson and JSON.</p>
 */
public final class ObservationPayloadJsonCodec {

    private final ObjectMapper objectMapper;

    public ObservationPayloadJsonCodec(
            ObjectMapper objectMapper
    ) {
        this.objectMapper = Objects.requireNonNull(
                objectMapper,
                "objectMapper must not be null"
        );
    }

    public String encode(
            ObservationPayload payload
    ) {
        Objects.requireNonNull(
                payload,
                "payload must not be null"
        );

        ArrayNode root =
                objectMapper.createArrayNode();

        for (ObservationField field :
                payload.asList()) {

            ObjectNode fieldNode =
                    root.addObject();

            fieldNode.put(
                    "name",
                    field.name()
            );

            fieldNode.set(
                    "value",
                    encodeValue(field.value())
            );
        }

        try {
            return objectMapper.writeValueAsString(root);
        } catch (JacksonException exception) {
            throw new IllegalStateException(
                    "Failed to serialize observation payload",
                    exception
            );
        }
    }

    public ObservationPayload decode(
            String json
    ) {
        Objects.requireNonNull(
                json,
                "json must not be null"
        );

        try {
            JsonNode root =
                    objectMapper.readTree(json);

            if (!root.isArray()) {
                throw new IllegalStateException(
                        "Persisted observation payload must be a JSON array"
                );
            }

            List<ObservationField> fields =
                    new ArrayList<>();

            for (JsonNode fieldNode : root) {
                String name =
                        requiredText(
                                fieldNode,
                                "name"
                        );

                JsonNode valueNode =
                        fieldNode.get("value");

                if (valueNode == null) {
                    throw new IllegalStateException(
                            "Persisted observation field must contain value"
                    );
                }

                fields.add(
                        ObservationField.of(
                                name,
                                decodeValue(valueNode)
                        )
                );
            }

            if (fields.isEmpty()) {
                throw new IllegalStateException(
                        "Persisted observation payload must not be empty"
                );
            }

            return new ObservationPayload(
                    new com.fold.kernel.collections.NonEmptyList<>(
                            fields
                    )
            );
        } catch (JacksonException exception) {
            throw new IllegalStateException(
                    "Failed to deserialize observation payload",
                    exception
            );
        }
    }

    private ObjectNode encodeValue(
            ObservationValue value
    ) {
        ObjectNode node =
                objectMapper.createObjectNode();

        switch (value) {
            case ObservationValue.Text text -> {
                node.put("type", "text");
                node.put("value", text.value());
            }

            case ObservationValue.Number number -> {
                node.put("type", "number");
                node.put(
                        "value",
                        number.value()
                );
            }

            case ObservationValue.BooleanValue booleanValue -> {
                node.put("type", "boolean");
                node.put(
                        "value",
                        booleanValue.value()
                );
            }

            case ObservationValue.TimestampValue timestamp -> {
                node.put("type", "timestamp");
                node.put(
                        "value",
                        timestamp.value().toString()
                );
            }

            case ObservationValue.Identifier identifier -> {
                node.put("type", "identifier");
                node.put(
                        "scheme",
                        identifier.scheme()
                );
                node.put(
                        "value",
                        identifier.value()
                );
            }

            case ObservationValue.ListValue list -> {
                node.put("type", "list");

                ArrayNode values =
                        node.putArray("values");

                for (ObservationValue nested :
                        list.values()) {
                    values.add(
                            encodeValue(nested)
                    );
                }
            }
        }

        return node;
    }

    private ObservationValue decodeValue(
            JsonNode node
    ) {
        String type =
                requiredText(node, "type");

        return switch (type) {
            case "text" -> ObservationValue.Text.of(
                    requiredTextAllowEmpty(
                            node,
                            "value"
                    )
            );

            case "number" -> {
                JsonNode value =
                        requiredNode(node, "value");

                if (!value.isNumber()) {
                    throw new IllegalStateException(
                            "Persisted number observation value must be numeric"
                    );
                }

                yield ObservationValue.Number.of(
                        value.decimalValue()
                );
            }

            case "boolean" -> {
                JsonNode value =
                        requiredNode(node, "value");

                if (!value.isBoolean()) {
                    throw new IllegalStateException(
                            "Persisted boolean observation value must be boolean"
                    );
                }

                yield ObservationValue.BooleanValue.of(
                        value.booleanValue()
                );
            }

            case "timestamp" -> ObservationValue.TimestampValue.of(
                    Timestamp.parse(
                            requiredText(
                                    node,
                                    "value"
                            )
                    )
            );

            case "identifier" -> ObservationValue.Identifier.of(
                    requiredText(
                            node,
                            "scheme"
                    ),
                    requiredText(
                            node,
                            "value"
                    )
            );

            case "list" -> {
                JsonNode valuesNode =
                        requiredNode(
                                node,
                                "values"
                        );

                if (!valuesNode.isArray()
                        || valuesNode.isEmpty()) {
                    throw new IllegalStateException(
                            "Persisted list observation value must be a non-empty array"
                    );
                }

                List<ObservationValue> values =
                        new ArrayList<>();

                for (JsonNode nested :
                        valuesNode) {
                    values.add(
                            decodeValue(nested)
                    );
                }

                yield new ObservationValue.ListValue(
                        new com.fold.kernel.collections.NonEmptyList<>(
                                values
                        )
                );
            }

            default -> throw new IllegalStateException(
                    "Unknown persisted observation value type: "
                            + type
            );
        };
    }

    private static JsonNode requiredNode(
            JsonNode node,
            String field
    ) {
        JsonNode value =
                node.get(field);

        if (value == null || value.isNull()) {
            throw new IllegalStateException(
                    "Missing persisted field: " + field
            );
        }

        return value;
    }

    private static String requiredText(
            JsonNode node,
            String field
    ) {
        String value =
                requiredTextAllowEmpty(
                        node,
                        field
                );

        if (value.isBlank()) {
            throw new IllegalStateException(
                    "Persisted field must not be blank: "
                            + field
            );
        }

        return value;
    }

    private static String requiredTextAllowEmpty(
            JsonNode node,
            String field
    ) {
        JsonNode value =
                requiredNode(node, field);

        if (!value.isTextual()) {
            throw new IllegalStateException(
                    "Persisted field must be textual: "
                            + field
            );
        }

        return value.stringValue();
    }
}