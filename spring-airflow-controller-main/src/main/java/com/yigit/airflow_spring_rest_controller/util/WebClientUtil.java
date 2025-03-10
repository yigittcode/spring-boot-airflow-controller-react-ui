package com.yigit.airflow_spring_rest_controller.util;

import com.yigit.airflow_spring_rest_controller.exception.AirflowBadRequestException;
import com.yigit.airflow_spring_rest_controller.exception.AirflowConflictException;
import com.yigit.airflow_spring_rest_controller.exception.AirflowResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * Utility class for WebClient operations with comprehensive null safety and error handling.
 * Provides standardized HTTP request execution with consistent behavior across all HTTP methods.
 */
public class WebClientUtil {
    
    private static final Logger log = LoggerFactory.getLogger(WebClientUtil.class);

    /**
     * Execute GET request with comprehensive error handling
     * 
     * @param webClient The WebClient instance to use for the request
     * @param path The API path to request
     * @param pathVariables Variables to be substituted in the path
     * @param queryParams Query parameters to add to the request
     * @param responseType The expected response type
     * @param resourceName Human-readable name of the resource for error messages
     * @return A Mono containing the response
     * @throws NullPointerException If any required parameter is null
     * @throws IllegalArgumentException If path is empty
     */
    public static <T> Mono<T> get(
            WebClient webClient,
            String path,
            Map<String, Object> pathVariables,
            Map<String, String> queryParams,
            Class<T> responseType,
            String resourceName) {

        // Validate required parameters
        Objects.requireNonNull(webClient, "WebClient cannot be null");
        Objects.requireNonNull(path, "Path cannot be null");
        if (path.trim().isEmpty()) {
            throw new IllegalArgumentException("Path cannot be empty");
        }
        Objects.requireNonNull(responseType, "Response type cannot be null");
        Objects.requireNonNull(resourceName, "Resource name cannot be null");
        
        // Use empty map if pathVariables is null
        final Map<String, Object> safePathVars = pathVariables != null ? 
                pathVariables : Collections.emptyMap();
                
        log.debug("Executing GET request to {}, pathVars: {}, queryParams: {}", path, safePathVars, queryParams);
        
        return webClient.get()
                .uri(uriBuilder -> {
                    var builder = uriBuilder.path(path);
                    
                    if (queryParams != null) {
                        queryParams.forEach((key, value) -> {
                            if (value != null) {
                                builder.queryParam(key, value);
                            }
                        });
                    }
                    
                    return builder.build(safePathVars);
                })
                .retrieve()
                .onStatus(
                    status -> status.value() == HttpStatus.NOT_FOUND.value(),
                    response -> {
                        log.warn("Resource not found: {} at path: {}", resourceName, path);
                        return Mono.error(new AirflowResourceNotFoundException(resourceName + " not found"));
                    }
                )
                .onStatus(
                    status -> status.is5xxServerError(),
                    response -> {
                        log.error("Server error occurred for resource: {} at path: {}, status: {}", 
                            resourceName, path, response.statusCode());
                        return Mono.error(new RuntimeException("Server error: " + response.statusCode()));
                    }
                )
                .bodyToMono(responseType)
                .doOnSuccess(result -> log.debug("GET request to {} completed successfully", path))
                .doOnError(error -> log.error("GET request to {} failed: {}", path, error.getMessage()));
    }

    /**
     * Execute POST request with comprehensive error handling
     * 
     * @param webClient The WebClient instance to use for the request
     * @param path The API path to request
     * @param pathVariables Variables to be substituted in the path
     * @param body Request body to send
     * @param responseType The expected response type
     * @param resourceName Human-readable name of the resource for error messages
     * @return A Mono containing the response
     * @throws NullPointerException If any required parameter is null
     * @throws IllegalArgumentException If path is empty
     */
    public static <T> Mono<T> post(
            WebClient webClient,
            String path,
            Map<String, Object> pathVariables,
            Object body,
            Class<T> responseType,
            String resourceName) {

        // Validate required parameters
        Objects.requireNonNull(webClient, "WebClient cannot be null");
        Objects.requireNonNull(path, "Path cannot be null");
        if (path.trim().isEmpty()) {
            throw new IllegalArgumentException("Path cannot be empty");
        }
        Objects.requireNonNull(body, "Request body cannot be null for POST request");
        Objects.requireNonNull(responseType, "Response type cannot be null");
        Objects.requireNonNull(resourceName, "Resource name cannot be null");
        
        // Use empty map if pathVariables is null
        final Map<String, Object> safePathVars = pathVariables != null ? 
                pathVariables : Collections.emptyMap();
        
        log.debug("Executing POST request to {}, pathVars: {}", path, safePathVars);
        
        return webClient.post()
                .uri(uriBuilder -> {
                    var builder = uriBuilder.path(path);
                    return builder.build(safePathVars);
                })
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .onStatus(
                    status -> status.value() == HttpStatus.NOT_FOUND.value(),
                    response -> {
                        log.warn("Resource not found: {} at path: {}", resourceName, path);
                        return Mono.error(new AirflowResourceNotFoundException(resourceName + " not found"));
                    }
                )
                .onStatus(
                    status -> status.value() == HttpStatus.BAD_REQUEST.value(),
                    response -> {
                        log.warn("Bad request for resource: {} at path: {}", resourceName, path);
                        return Mono.error(new AirflowBadRequestException("Invalid request parameters"));
                    }
                )
                .onStatus(
                    status -> status.value() == HttpStatus.CONFLICT.value(),
                    response -> {
                        log.warn("Conflict for resource: {} at path: {}", resourceName, path);
                        return Mono.error(new AirflowConflictException("Operation resulted in conflict"));
                    }
                )
                .onStatus(
                    status -> status.is5xxServerError(),
                    response -> {
                        log.error("Server error occurred for resource: {} at path: {}, status: {}", 
                            resourceName, path, response.statusCode());
                        return Mono.error(new RuntimeException("Server error: " + response.statusCode()));
                    }
                )
                .bodyToMono(responseType)
                .doOnSuccess(result -> log.debug("POST request to {} completed successfully", path))
                .doOnError(error -> log.error("POST request to {} failed: {}", path, error.getMessage()));
    }

    /**
     * Execute PATCH request with comprehensive error handling
     * 
     * @param webClient The WebClient instance to use for the request
     * @param path The API path to request
     * @param pathVariables Variables to be substituted in the path
     * @param body Request body to send
     * @param responseType The expected response type
     * @param resourceName Human-readable name of the resource for error messages
     * @return A Mono containing the response
     * @throws NullPointerException If any required parameter is null
     * @throws IllegalArgumentException If path is empty
     */
    public static <T> Mono<T> patch(
            WebClient webClient,
            String path,
            Map<String, Object> pathVariables,
            Object body,
            Class<T> responseType,
            String resourceName) {

        // Validate required parameters
        Objects.requireNonNull(webClient, "WebClient cannot be null");
        Objects.requireNonNull(path, "Path cannot be null");
        if (path.trim().isEmpty()) {
            throw new IllegalArgumentException("Path cannot be empty");
        }
        Objects.requireNonNull(body, "Request body cannot be null for PATCH request");
        Objects.requireNonNull(responseType, "Response type cannot be null");
        Objects.requireNonNull(resourceName, "Resource name cannot be null");
        
        // Use empty map if pathVariables is null
        final Map<String, Object> safePathVars = pathVariables != null ? 
                pathVariables : Collections.emptyMap();
        
        log.debug("Executing PATCH request to {}, pathVars: {}", path, safePathVars);
        
        return webClient.patch()
                .uri(uriBuilder -> {
                    var builder = uriBuilder.path(path);
                    return builder.build(safePathVars);
                })
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .onStatus(
                    status -> status.value() == HttpStatus.NOT_FOUND.value(),
                    response -> {
                        log.warn("Resource not found: {} at path: {}", resourceName, path);
                        return Mono.error(new AirflowResourceNotFoundException(resourceName + " not found"));
                    }
                )
                .onStatus(
                    status -> status.value() == HttpStatus.BAD_REQUEST.value(),
                    response -> {
                        log.warn("Bad request for resource: {} at path: {}", resourceName, path);
                        return Mono.error(new AirflowBadRequestException("Invalid request parameters"));
                    }
                )
                .onStatus(
                    status -> status.value() == HttpStatus.CONFLICT.value(),
                    response -> {
                        log.warn("Conflict for resource: {} at path: {}", resourceName, path);
                        return Mono.error(new AirflowConflictException("Update conflict"));
                    }
                )
                .onStatus(
                    status -> status.is5xxServerError(),
                    response -> {
                        log.error("Server error occurred for resource: {} at path: {}, status: {}", 
                            resourceName, path, response.statusCode());
                        return Mono.error(new RuntimeException("Server error: " + response.statusCode()));
                    }
                )
                .bodyToMono(responseType)
                .doOnSuccess(result -> log.debug("PATCH request to {} completed successfully", path))
                .doOnError(error -> log.error("PATCH request to {} failed: {}", path, error.getMessage()));
    }

    /**
     * Execute DELETE request with comprehensive error handling
     * 
     * @param webClient The WebClient instance to use for the request
     * @param path The API path to request
     * @param pathVariables Variables to be substituted in the path
     * @param resourceName Human-readable name of the resource for error messages
     * @return A Mono that completes when the delete operation is successful
     * @throws NullPointerException If any required parameter is null
     * @throws IllegalArgumentException If path is empty
     */
    public static Mono<Void> delete(
            WebClient webClient,
            String path,
            Map<String, Object> pathVariables,
            String resourceName) {

        // Validate required parameters
        Objects.requireNonNull(webClient, "WebClient cannot be null");
        Objects.requireNonNull(path, "Path cannot be null");
        if (path.trim().isEmpty()) {
            throw new IllegalArgumentException("Path cannot be empty");
        }
        Objects.requireNonNull(resourceName, "Resource name cannot be null");
        
        // Use empty map if pathVariables is null
        final Map<String, Object> safePathVars = pathVariables != null ? 
                pathVariables : Collections.emptyMap();
        
        log.debug("Executing DELETE request to {}, pathVars: {}", path, safePathVars);
        
        return webClient.delete()
                .uri(uriBuilder -> {
                    var builder = uriBuilder.path(path);
                    return builder.build(safePathVars);
                })
                .retrieve()
                .onStatus(
                    status -> status.value() == HttpStatus.NOT_FOUND.value(),
                    response -> {
                        log.warn("Resource not found: {} at path: {}", resourceName, path);
                        return Mono.error(new AirflowResourceNotFoundException(resourceName + " not found"));
                    }
                )
                .onStatus(
                    status -> status.value() == HttpStatus.CONFLICT.value(),
                    response -> {
                        log.warn("Conflict for resource: {} at path: {}", resourceName, path);
                        return Mono.error(new AirflowConflictException("Cannot delete resource"));
                    }
                )
                .onStatus(
                    status -> status.is5xxServerError(),
                    response -> {
                        log.error("Server error occurred for resource: {} at path: {}, status: {}", 
                            resourceName, path, response.statusCode());
                        return Mono.error(new RuntimeException("Server error: " + response.statusCode()));
                    }
                )
                .bodyToMono(Void.class)
                .doOnSuccess(result -> log.debug("DELETE request to {} completed successfully", path))
                .doOnError(error -> log.error("DELETE request to {} failed: {}", path, error.getMessage()));
    }
} 