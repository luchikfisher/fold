package com.fold.modules.observations.application.query;

import com.fold.modules.observations.api.view.ObservationFieldView;
import com.fold.modules.observations.api.view.ObservationValueView;
import com.fold.modules.observations.api.view.ObservationView;
import com.fold.modules.observations.domain.model.Observation;
import com.fold.modules.observations.domain.value.ObservationValue;

final class ObservationViewMapper {

    private ObservationViewMapper() {
    }

    static ObservationView map(
            Observation observation
    ) {
        return new ObservationView(
                observation.id(),
                observation.type().value(),
                new ObservationView.Subject(
                        observation.subject().type(),
                        observation.subject().externalKey()
                ),
                observation.payload()
                        .asList()
                        .stream()
                        .map(field ->
                                new ObservationFieldView(
                                        field.name(),
                                        mapValue(field.value())
                                )
                        )
                        .toList(),
                new ObservationView.Origin(
                        observation.origin().sourceId(),
                        observation.origin()
                                .evidence()
                                .orElse(null),
                        observation.origin()
                                .externalRecord()
                                .orElse(null)
                ),
                new ObservationView.Fingerprint(
                        observation.fingerprint().version(),
                        observation.fingerprint().algorithm(),
                        observation.fingerprint().value()
                ),
                observation.observedAt().value(),
                observation.arrivedAt().value(),
                observation.status().name(),
                observation.decidedAt().orElse(null),
                observation.rejection()
                        .map(rejection ->
                                new ObservationView.Rejection(
                                        rejection.code(),
                                        rejection.message()
                                )
                        )
                        .orElse(null)
        );
    }

    private static ObservationValueView mapValue(
            ObservationValue value
    ) {
        return switch (value) {
            case ObservationValue.Text text -> new ObservationValueView.Text(
                    text.value()
            );

            case ObservationValue.Number number -> new ObservationValueView.Number(
                    number.value()
            );

            case ObservationValue.BooleanValue booleanValue -> new ObservationValueView.BooleanValue(
                    booleanValue.value()
            );

            case ObservationValue.TimestampValue timestamp -> new ObservationValueView.TimestampValue(
                    timestamp.value()
            );

            case ObservationValue.Identifier identifier -> new ObservationValueView.Identifier(
                    identifier.scheme(),
                    identifier.value()
            );

            case ObservationValue.ListValue list -> new ObservationValueView.ListValue(
                    list.values()
                            .stream()
                            .map(
                                    ObservationViewMapper::mapValue
                            )
                            .toList()
            );
        };
    }
}