package com.yigit.airflow_spring_rest_controller.service;

import com.yigit.airflow_spring_rest_controller.dto.log.DagActionLogDTO;
import com.yigit.airflow_spring_rest_controller.dto.log.DagActionLogResponse;
import com.yigit.airflow_spring_rest_controller.entity.DagActionLog;
import com.yigit.airflow_spring_rest_controller.entity.DagActionLog.ActionType;
import com.yigit.airflow_spring_rest_controller.repository.DagActionLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.reactive.function.server.ServerRequest;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DagActionLogService {

    private final DagActionLogRepository dagActionLogRepository;

    /**
     * Logs a DAG action with the currently authenticated user
     */
    public Mono<DagActionLog> logDagAction(String dagId, ActionType actionType, String actionDetails, Boolean success, String runId) {
        logAuthenticationDetails();
        
        return ReactiveSecurityContextHolder.getContext()
                .doOnNext(securityContext -> {
                    if (securityContext.getAuthentication() != null) {
                        System.out.println("AUTH USERNAME: " + securityContext.getAuthentication().getName());
                        System.out.println("AUTH PRINCIPAL: " + securityContext.getAuthentication().getPrincipal());
                        System.out.println("AUTH CREDENTIALS: " + (securityContext.getAuthentication().getCredentials() != null ? "Present" : "Not present"));
                        System.out.println("AUTH AUTHORITIES: " + securityContext.getAuthentication().getAuthorities());
                    } else {
                        System.out.println("No authentication found in SecurityContext");
                    }
                })
                .map(SecurityContext::getAuthentication)
                .map(Authentication::getName)
                .defaultIfEmpty("unknown") // Ensure we always have a username
                .flatMap(username -> {
                    LocalDateTime now = LocalDateTime.now();
                    String actionTypeValue = actionType.getValue();
                    
                    // Ensure parameters are never null
                    String safeUsername = username != null ? username : "unknown";
                    System.out.println("LOGGING DAG ACTION FOR USER: " + safeUsername);
                    String safeDagId = dagId != null ? dagId : "";
                    String safeActionType = actionTypeValue != null ? actionTypeValue : "OTHER";
                    String safeActionDetails = actionDetails != null ? actionDetails : "";
                    Boolean safeSuccess = success != null ? success : true;
                    
                    // First insert using the custom method
                    return dagActionLogRepository.insertDagActionLog(
                            safeUsername,
                            safeDagId,
                            safeActionType,
                            safeActionDetails,
                            now,
                            safeSuccess,
                            runId // runId can be null, SQL will handle it
                    ).then(
                        // Then build and return a DagActionLog object for the caller
                        Mono.just(DagActionLog.builder()
                            .username(safeUsername)
                            .dagId(safeDagId)
                            .actionType(safeActionType)
                            .actionDetails(safeActionDetails)
                            .timestamp(now)
                            .success(safeSuccess)
                            .runId(runId)
                            .build())
                    );
                });
    }

    /**
     * Helper method to log authentication details for debugging
     */
    private void logAuthenticationDetails() {
        System.out.println("------- AUTHENTICATION DEBUG INFO -------");
        ReactiveSecurityContextHolder.getContext()
            .map(SecurityContext::getAuthentication)
            .subscribe(
                auth -> {
                    if (auth != null) {
                        System.out.println("AUTHENTICATION: " + auth.getClass().getName());
                        System.out.println("USERNAME: " + auth.getName());
                        System.out.println("PRINCIPAL: " + auth.getPrincipal());
                        System.out.println("CREDENTIALS: " + (auth.getCredentials() != null ? "Present" : "Not present"));
                        System.out.println("AUTHENTICATED: " + auth.isAuthenticated());
                        System.out.println("AUTHORITIES: " + auth.getAuthorities());
                    } else {
                        System.out.println("NO AUTHENTICATION OBJECT");
                    }
                },
                error -> System.out.println("ERROR GETTING AUTHENTICATION: " + error.getMessage()),
                () -> System.out.println("------- END DEBUG INFO -------")
            );
    }

    /**
     * Get all logs with pagination
     */
    public Mono<DagActionLogResponse> getAllLogs(int page, int size) {
        int offset = page * size;
        
        return dagActionLogRepository.countAll()
                .next()
                .flatMap(total -> 
                    dagActionLogRepository.findAllWithPagination(size, offset)
                            .map(this::mapToDTO)
                            .collectList()
                            .map(logs -> new DagActionLogResponse(logs, total, page, size))
                );
    }

    /**
     * Get logs for a specific DAG
     */
    public Flux<DagActionLogDTO> getLogsByDagId(String dagId) {
        return dagActionLogRepository.findByDagIdOrderByTimestampDesc(dagId)
                .map(this::mapToDTO);
    }

    /**
     * Get logs for a specific action type
     */
    public Flux<DagActionLogDTO> getLogsByActionType(String actionType) {
        return dagActionLogRepository.findByActionType(actionType)
                .map(this::mapToDTO);
    }

    /**
     * Convert entity to DTO
     */
    private DagActionLogDTO mapToDTO(DagActionLog entity) {
        return DagActionLogDTO.builder()
                .id(entity.getId())
                .username(entity.getUsername())
                .dagId(entity.getDagId())
                .actionType(entity.getActionType())
                .actionDetails(entity.getActionDetails())
                .timestamp(entity.getTimestamp())
                .success(entity.getSuccess())
                .runId(entity.getRunId())
                .build();
    }
} 