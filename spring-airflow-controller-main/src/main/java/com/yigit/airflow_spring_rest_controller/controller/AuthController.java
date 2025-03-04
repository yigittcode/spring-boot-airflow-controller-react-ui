package com.yigit.airflow_spring_rest_controller.controller;

import com.yigit.airflow_spring_rest_controller.model.AuthResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ResponseStatus;
import reactor.core.publisher.Mono;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Operations related to user authentication")
public class AuthController {

    @Operation(
        summary = "Verify User Credentials",
        description = "Endpoint to verify user authentication. If the request reaches this endpoint, it implies successful authentication."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Authentication successful"),
        @ApiResponse(responseCode = "401", description = "Authentication failed (handled by Spring Security)")
    })
    @PostMapping("/verify")
    @ResponseStatus(HttpStatus.OK)
    public Mono<AuthResponse> verifyCredentials() {
        // If this endpoint is reached, authentication was successful
        // Spring Security will handle the 401 if credentials are invalid
        return Mono.just(new AuthResponse("Authentication successful"));
    }
}