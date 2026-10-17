package com.fold.modules.claims.infrastructure.persistence.mapper;

import com.fold.kernel.time.Timestamp;
import com.fold.modules.claims.domain.value.ClaimObject;
import com.fold.modules.claims.domain.value.ClaimReference;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Durable JSON persistence codec for ClaimObject.
 *
 * <p>The JSON representation is infrastructure state, not the domain model.</p>
 */
public final class ClaimObjectJsonCodec {

    private final ObjectMapper objectMapper;

    public ClaimObjectJsonCodec(
            ObjectMapper objectMapper
    ) {
        this.objectMapper = Objects.requireNonNull(
                objectMapper,
                "objectMapper must not be null"
        );
    }

    public String encode(
            ClaimObject object
    ) {
        Objects.requireNonNull(
                object,
                "object must not be null"
        );

        Map<String, Object> json =
                new LinkedHashMap<>();

        switch (object) {
            case ClaimObject.Reference reference -> {
                json.put("type", "reference");
                json.put(
                        "namespace",
                        reference.value().namespace()
                );
                json.put(
                        "value",
                        reference.value().value()
                );
            }

            case ClaimObject.Text text -> {
                json.put("type", "text");
                json.put("value", text.value());
            }

            case ClaimObject.NumberValue number -> {
                json.put("type", "number");
                json.put(
                        "value",
                        number.value().toPlainString()
                );
            }

            case ClaimObject.BooleanValue booleanValue -> {
                json.put("type", "boolean");
                json.put(
                        "value",
                        booleanValue.value()
                );
            }

            case ClaimObject.TimestampValue timestamp -> {
                json.put("type", "timestamp");
                json.put(
                        "value",
                        timestamp.value().toString()
                );
            }

            case ClaimObject.Identifier identifier -> {
                json.put("type", "identifier");
                json.put(
                        "scheme",
                        identifier.scheme()
                );
                json.put(
                        "value",
                        identifier.value()
                );
            }
        }

        try {
            return objectMapper.writeValueAsString(
                    json
            );
        } catch (JacksonException exception) {
            throw new IllegalStateException(
                    "Failed to encode ClaimObject",
                    exception
            );
        }
    }

    public ClaimObject decode(
            String json
    ) {
        Objects.requireNonNull(
                json,
                "json must not be null"
        );

        try {
            JsonNode root =
                    objectMapper.readTree(json);

            String type =
                    requiredString(
                            root,
                            "type"
                    );

            return switch (type) {
                case "reference" ->
                        new ClaimObject.Reference(
                                new ClaimReference(
                                        requiredString(
                                                root,
                                                "namespace"
                                        ),
                                        requiredString(
                                                root,
                                                "value"
                                        )
                                )
                        );

                case "text" ->
                        new ClaimObject.Text(
                                requiredString(
                                        root,
                                        "value"
                                )
                        );

                case "number" ->
                        new ClaimObject.NumberValue(
                                new BigDecimal(
                                        requiredString(
                                                root,
                                                "value"
                                        )
                                )
                        );

                case "boolean" ->
                        new ClaimObject.BooleanValue(
                                requiredBoolean(
                                        root,
                                        "value"
                                )
                        );

                case "timestamp" ->
                        new ClaimObject.TimestampValue(
                                Timestamp.parse(
                                        requiredString(
                                                root,
                                                "value"
                                        )
                                )
                        );

                case "identifier" ->
                        new ClaimObject.Identifier(
                                requiredString(
                                        root,
                                        "scheme"
                                ),
                                requiredString(
                                        root,
                                        "value"
                                )
                        );

                default ->
                        throw new IllegalArgumentException(
                                "Unsupported persisted ClaimObject type: "
                                        + type
                        );
            };

        } catch (JacksonException exception) {
            throw new IllegalStateException(
                    "Failed to decode ClaimObject",
                    exception
            );
        }
    }

    private static String requiredString(
            JsonNode node,
            String field
    ) {
        JsonNode value =
                node.get(field);

        if (value == null
                || !value.isString()) {
            throw new IllegalArgumentException(
                    "ClaimObject field '"
                            + field
                            + "' must be a string"
            );
        }

        return value.stringValue();
    }

    private static boolean requiredBoolean(
            JsonNode node,
            String field
    ) {
        JsonNode value =
                node.get(field);

        if (value == null
                || !value.isBoolean()) {
            throw new IllegalArgumentException(
                    "ClaimObject field '"
                            + field
                            + "' must be a boolean"
            );
        }

        return value.booleanValue();
    }
}