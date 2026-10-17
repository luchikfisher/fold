package com.fold.modules.claims.application.integration.observations;

import com.fold.modules.claims.domain.derivation.ClaimDerivationField;
import com.fold.modules.claims.domain.derivation.ClaimDerivationInput;
import com.fold.modules.claims.domain.derivation.ClaimDerivationOrigin;
import com.fold.modules.claims.domain.derivation.ClaimDerivationSubject;
import com.fold.modules.claims.domain.derivation.ClaimDerivationValue;
import com.fold.modules.observations.api.view.ObservationFieldView;
import com.fold.modules.observations.api.view.ObservationValueView;
import com.fold.modules.observations.api.view.ObservationView;

import java.util.List;
import java.util.Objects;

/**
 * Translates the public Observations read contract into Claims-owned
 * derivation input.
 *
 * <p>This is an anti-corruption boundary. Observations API types stop here and
 * do not enter the Claims domain.</p>
 */
public final class ObservationClaimDerivationInputMapper {

    /**
     * Maps one authoritative Observation view into Claims-owned semantic input.
     */
    public ClaimDerivationInput map(
            ObservationView observation
    ) {
        Objects.requireNonNull(
                observation,
                "observation must not be null"
        );

        List<ClaimDerivationField> fields =
                observation.payload()
                        .stream()
                        .map(this::mapField)
                        .toList();

        return new ClaimDerivationInput(
                observation.id(),
                observation.type(),
                new ClaimDerivationSubject(
                        observation.subject().type(),
                        observation.subject().externalKey()
                ),
                fields,
                new ClaimDerivationOrigin(
                        observation.id(),
                        observation.origin().sourceId(),
                        observation.origin().evidenceId(),
                        observation.origin().externalRecordId()
                ),
                observation.observedAt()
        );
    }

    private ClaimDerivationField mapField(
            ObservationFieldView field
    ) {
        return new ClaimDerivationField(
                field.name(),
                mapValue(field.value())
        );
    }

    private ClaimDerivationValue mapValue(
            ObservationValueView value
    ) {
        Objects.requireNonNull(
                value,
                "value must not be null"
        );

        if (value instanceof ObservationValueView.Text text) {
            return new ClaimDerivationValue.Text(
                    text.value()
            );
        }

        if (value instanceof ObservationValueView.Number number) {
            return new ClaimDerivationValue.NumberValue(
                    number.value()
            );
        }

        if (value instanceof ObservationValueView.BooleanValue booleanValue) {
            return new ClaimDerivationValue.BooleanValue(
                    booleanValue.value()
            );
        }

        if (value instanceof ObservationValueView.TimestampValue timestamp) {
            return new ClaimDerivationValue.TimestampValue(
                    timestamp.value()
            );
        }

        if (value instanceof ObservationValueView.Identifier identifier) {
            return new ClaimDerivationValue.Identifier(
                    identifier.scheme(),
                    identifier.value()
            );
        }

        if (value instanceof ObservationValueView.ListValue list) {
            return new ClaimDerivationValue.ListValue(
                    list.values()
                            .stream()
                            .map(this::mapValue)
                            .toList()
            );
        }

        throw new IllegalArgumentException(
                "Unsupported Observation value type: "
                        + value.getClass().getName()
        );
    }
}