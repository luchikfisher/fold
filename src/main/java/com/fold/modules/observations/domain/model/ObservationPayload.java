package com.fold.modules.observations.domain.model;

import com.fold.kernel.collections.NonEmptyList;

import java.util.*;

/**
 * Represents the normalized semantic fields carried by an observation.
 *
 * <p>A payload is non-empty and contains at most one field for each semantic
 * field name. It is immutable after creation.</p>
 *
 * <p>The payload represents normalized information, not raw source data.
 * Original source material belongs to the evidence layer and is referenced
 * through the observation origin.</p>
 *
 * @param fields the semantic observation fields
 */
public record ObservationPayload(
        NonEmptyList<ObservationField> fields
) {

    public ObservationPayload {
        Objects.requireNonNull(fields, "fields must not be null");
        requireUniqueFieldNames(fields);
    }

    public static ObservationPayload of(
            ObservationField first,
            ObservationField... remaining
    ) {
        return new ObservationPayload(
                NonEmptyList.of(first, remaining)
        );
    }

    /**
     * Returns all fields in stable encounter order.
     *
     * @return the immutable fields
     */
    public List<ObservationField> asList() {
        return fields.asList();
    }

    /**
     * Returns a field by its exact semantic name.
     *
     * @param name the semantic field name
     * @return the field, if present
     */
    public Optional<ObservationField> find(String name) {
        Objects.requireNonNull(name, "name must not be null");

        return fields.stream()
                .filter(field -> field.name().equals(name))
                .findFirst();
    }

    public boolean contains(String name) {
        return find(name).isPresent();
    }

    public int size() {
        return fields.size();
    }

    private static void requireUniqueFieldNames(
            NonEmptyList<ObservationField> fields
    ) {
        Set<String> names = new HashSet<>();

        for (ObservationField field : fields) {
            if (!names.add(field.name())) {
                throw new IllegalArgumentException(
                        "payload must not contain duplicate field name: "
                                + field.name()
                );
            }
        }
    }
}