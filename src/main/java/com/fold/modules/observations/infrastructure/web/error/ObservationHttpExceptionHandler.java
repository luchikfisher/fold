package com.fold.modules.observations.infrastructure.web.error;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public final class ObservationHttpExceptionHandler {

    @ExceptionHandler(
            InvalidObservationRequestException.class
    )
    public ResponseEntity<ProblemDetail> invalidObservation(
            InvalidObservationRequestException exception
    ) {
        ProblemDetail problem =
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.BAD_REQUEST,
                        exception.getMessage()
                );

        problem.setTitle(
                "Invalid observation request"
        );

        return ResponseEntity
                .badRequest()
                .body(problem);
    }

    @ExceptionHandler(
            HttpMessageNotReadableException.class
    )
    public ResponseEntity<ProblemDetail> malformedJson(
            HttpMessageNotReadableException exception
    ) {
        ProblemDetail problem =
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.BAD_REQUEST,
                        "The request body is not valid JSON for this endpoint"
                );

        problem.setTitle(
                "Malformed request body"
        );

        return ResponseEntity
                .badRequest()
                .body(problem);
    }
}