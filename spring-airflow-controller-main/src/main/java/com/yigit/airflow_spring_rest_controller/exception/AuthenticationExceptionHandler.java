package com.yigit.airflow_spring_rest_controller.exception;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

@RestControllerAdvice
public class AuthenticationExceptionHandler {

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Mono<ErrorResponse> handleAuthenticationException(AuthenticationException ex) {
        return Mono.just(new ErrorResponse(
            HttpStatus.UNAUTHORIZED.value(),
            "Authentication Failed",
            "Invalid username or password"
        ));
    }
} 