package com.yigit.airflow_spring_rest_controller.exception;

import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(AirflowResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Mono<ErrorResponse> handleResourceNotFoundException(AirflowResourceNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        return createErrorResponse(HttpStatus.NOT_FOUND, "Resource Not Found", ex.getMessage());
    }

    @ExceptionHandler(AirflowConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Mono<ErrorResponse> handleConflictException(AirflowConflictException ex) {
        log.warn("Conflict occurred: {}", ex.getMessage());
        return createErrorResponse(HttpStatus.CONFLICT, "Conflict", ex.getMessage());
    }
    
    @ExceptionHandler(AirflowBadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Mono<ErrorResponse> handleBadRequestException(AirflowBadRequestException ex) {
        log.warn("Bad request: {}", ex.getMessage());
        return createErrorResponse(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage());
    }

    @ExceptionHandler(WebClientResponseException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleWebClientResponseException(WebClientResponseException ex) {
        log.error("Airflow API error: {}, status: {}", ex.getMessage(), ex.getStatusCode());
        ErrorResponse errorResponse = new ErrorResponse(
            ex.getStatusCode().value(),
            "Airflow API Error",
            ex.getResponseBodyAsString()
        );
        return Mono.just(ResponseEntity.status(ex.getStatusCode()).body(errorResponse));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public Mono<ErrorResponse> handleResponseStatusException(ResponseStatusException ex) {
        log.error("Response status exception: {}, status: {}", ex.getMessage(), ex.getStatusCode());
        return createErrorResponse(
            HttpStatus.valueOf(ex.getStatusCode().value()), 
            ex.getReason(), 
            ex.getMessage()
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Mono<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());
        return createErrorResponse(
            HttpStatus.FORBIDDEN, 
            "Access Denied", 
            "You don't have permission to access this resource"
        );
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Mono<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Unexpected error occurred: {}", ex.getMessage(), ex);
        return createErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR, 
            "Internal Server Error", 
            ex.getMessage()
        );
    }
    
    private Mono<ErrorResponse> createErrorResponse(HttpStatus status, String title, String detail) {
        return Mono.just(new ErrorResponse(status.value(), title, detail));
    }
} 