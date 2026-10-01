package com.fold.modules.observations.infrastructure.web.request;

import tools.jackson.databind.JsonNode;

import java.util.List;

/**
 * HTTP representation of one typed observation value.
 *
 * <p>The fields used depend on {@code type}:</p>
 *
 * <ul>
 *     <li>{@code text}: {@code value}</li>
 *     <li>{@code number}: {@code value}</li>
 *     <li>{@code boolean}: {@code value}</li>
 *     <li>{@code timestamp}: {@code value}</li>
 *     <li>{@code identifier}: {@code scheme} and {@code value}</li>
 *     <li>{@code list}: {@code values}</li>
 * </ul>
 */
public record ObservationValueRequest(
        String type,
        JsonNode value,
        String scheme,
        List<ObservationValueRequest> values
) {
}