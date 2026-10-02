package com.fold.modules.observations.infrastructure.web.controller;

import com.fold.kernel.ids.ObservationId;
import com.fold.modules.observations.api.query.GetObservationQuery;
import com.fold.modules.observations.application.feature.submitobservation.SubmitObservationResult;
import com.fold.modules.observations.application.feature.submitobservation.SubmitObservationUseCase;
import com.fold.modules.observations.application.query.GetObservationHandler;
import com.fold.modules.observations.infrastructure.web.error.InvalidObservationRequestException;
import com.fold.modules.observations.infrastructure.web.mapper.ObservationHttpMapper;
import com.fold.modules.observations.infrastructure.web.request.SubmitObservationRequest;
import com.fold.modules.observations.infrastructure.web.response.ObservationResponse;
import com.fold.modules.observations.infrastructure.web.response.SubmitObservationResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/observations")
public final class ObservationController {

    private final SubmitObservationUseCase submitObservation;
    private final GetObservationHandler getObservation;
    private final ObservationHttpMapper mapper;

    public ObservationController(
            SubmitObservationUseCase submitObservation,
            GetObservationHandler getObservation,
            ObservationHttpMapper mapper
    ) {
        this.submitObservation = submitObservation;
        this.getObservation = getObservation;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<SubmitObservationResponse> submit(
            @RequestBody SubmitObservationRequest request
    ) {
        SubmitObservationResult result =
                submitObservation.handle(
                        mapper.toCommand(request)
                );

        SubmitObservationResponse response =
                mapper.toResponse(result);

        URI location =
                URI.create(
                        "/api/v1/observations/"
                                + response.observationId()
                );

        if (result
                instanceof SubmitObservationResult.Duplicate) {
            return ResponseEntity
                    .ok()
                    .location(location)
                    .body(response);
        }

        return ResponseEntity
                .created(location)
                .body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ObservationResponse> get(
            @PathVariable String id
    ) {
        ObservationId observationId;

        try {
            observationId =
                    new ObservationId(
                            UUID.fromString(id)
                    );
        } catch (IllegalArgumentException exception) {
            throw new InvalidObservationRequestException(
                    "id must be a valid UUID",
                    exception
            );
        }

        return getObservation
                .handle(
                        new GetObservationQuery(
                                observationId
                        )
                )
                .map(mapper::toResponse)
                .map(ResponseEntity::ok)
                .orElseGet(
                        () -> ResponseEntity.notFound()
                                .build()
                );
    }
}