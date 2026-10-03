package com.fold.modules.claims.domain.derivation;

import com.fold.kernel.ids.ObservationId;
import com.fold.kernel.time.Timestamp;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Claims-owned semantic input used by explicit Claim derivation rules.
 *
 * <p>This type is intentionally independent of the Observations bounded
 * context implementation. The Claims application layer translates public
 * Observation API data into this representation before domain derivation
 * begins.</p>
 *
 * <p>Field order is preserved as received, but field lookup is semantic by
 * name. Duplicate field names are rejected because they would make explicit
 * derivation rules ambiguous.</p>
 */
public record ClaimDerivationInput(
        ObservationId observationId,
        String type,
        ClaimDerivationSubject subject,
        List<ClaimDerivationField> fields,
        ClaimDerivationOrigin origin,
        Timestamp observedAt
) {

    public ClaimDerivationInput {
        Objects.requireNonNull(
                observationId,
                "observationId must not be null"
        );

        Objects.requireNonNull(
                type,
                "type must not be null"
        );

        if (type.isBlank()) {
            throw new IllegalArgumentException(
                    "type must not be blank"
            );
        }

        Objects.requireNonNull(
                subject,
                "subject must not be null"
        );

        Objects.requireNonNull(
                fields,
                "fields must not be null"
        );

        Objects.requireNonNull(
                origin,
                "origin must not be null"
        );

        Objects.requireNonNull(
                observedAt,
                "observedAt must not be null"
        );

        if (!origin.observationId().equals(observationId)) {
            throw new IllegalArgumentException(
                    "origin ObservationId must match input ObservationId"
            );
        }

        if (fields.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException(
                    "fields must not contain null"
            );
        }

        Set<String> names =
                new HashSet<>();

        for (ClaimDerivationField field : fields) {
            if (!names.add(field.name())) {
                throw new IllegalArgumentException(
                        "fields must have unique names: "
                                + field.name()
                );
            }
        }

        fields = List.copyOf(fields);
    }

    /**
     * Retrieves one field by its exact semantic name.
     */
    public Optional<ClaimDerivationField> field(
            String name
    ) {
        Objects.requireNonNull(
                name,
                "name must not be null"
        );

        return fields.stream()
                .filter(field -> field.name().equals(name))
                .findFirst();
    }

    /**
     * Retrieves a field value by its exact semantic name.
     */
    public Optional<ClaimDerivationValue> value(
            String name
    ) {
        return field(name)
                .map(ClaimDerivationField::value);
    }
}