package com.yigit.airflow_spring_rest_controller.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AirflowResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Mono<ErrorResponse> handleResourceNotFoundException(AirflowResourceNotFoundException ex) {
        return Mono.just(new ErrorResponse(
            HttpStatus.NOT_FOUND.value(),
            "Resource Not Found",
            ex.getMessage()
        ));
    }

    @ExceptionHandler(AirflowConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Mono<ErrorResponse> handleConflictException(AirflowConflictException ex) {
        return Mono.just(new ErrorResponse(
            HttpStatus.CONFLICT.value(),
            "Conflict",
            ex.getMessage()
        ));
    }

    @ExceptionHandler(WebClientResponseException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleWebClientResponseException(WebClientResponseException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
            ex.getStatusCode().value(),
            "Airflow API Error",
            ex.getResponseBodyAsString()
        );
        return Mono.just(ResponseEntity.status(ex.getStatusCode()).body(errorResponse));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public Mono<ErrorResponse> handleResponseStatusException(ResponseStatusException ex) {
        return Mono.just(new ErrorResponse(
            ex.getStatusCode().value(),
            ex.getReason(),
            ex.getMessage()
        ));
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Mono<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex) {
        return Mono.just(new ErrorResponse(
            HttpStatus.FORBIDDEN.value(),
            "Access Denied",
            "You don't have permission to access this resource"
        ));
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Mono<ErrorResponse> handleGenericException(Exception ex) {
        return Mono.just(new ErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Internal Server Error",
            ex.getMessage()
        ));
    }
} 