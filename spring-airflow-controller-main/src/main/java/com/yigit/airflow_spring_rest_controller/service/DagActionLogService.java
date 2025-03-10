package com.yigit.airflow_spring_rest_controller.service;

import com.yigit.airflow_spring_rest_controller.dto.log.DagActionLogDTO;
import com.yigit.airflow_spring_rest_controller.dto.log.DagActionLogResponse;
import com.yigit.airflow_spring_rest_controller.entity.DagActionLog;
import com.yigit.airflow_spring_rest_controller.entity.DagActionLog.ActionType;
import com.yigit.airflow_spring_rest_controller.entity.Role;
import com.yigit.airflow_spring_rest_controller.repository.DagActionLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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
     * Helper method to check if the current user is an admin
     */
    private Mono<Boolean> isAdmin() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .map(auth -> auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }
    
    /**
     * Helper method to get the current username
     */
    private Mono<String> getCurrentUsername() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .map(Authentication::getName);
    }

    /**
     * Get all logs with pagination, filtered by user role
     */
    public Mono<DagActionLogResponse> getAllLogs(int page, int size) {
        int offset = page * size;
        
        return isAdmin()
                .flatMap(isAdmin -> {
                    if (isAdmin) {
                        // Admin can see all logs
                        return dagActionLogRepository.countAll()
                                .next()
                                .flatMap(total -> 
                                    dagActionLogRepository.findAllWithPagination(size, offset)
                                            .map(this::mapToDTO)
                                            .collectList()
                                            .map(logs -> new DagActionLogResponse(logs, total, page, size))
                                );
                    } else {
                        // Non-admin users can only see their own logs
                        return getCurrentUsername()
                                .flatMap(username -> 
                                    dagActionLogRepository.findByUsername(username)
                                            .map(this::mapToDTO)
                                            .collectList()
                                            .map(logs -> {
                                                int total = logs.size();
                                                int fromIndex = offset;
                                                int toIndex = Math.min(fromIndex + size, total);
                                                
                                                return new DagActionLogResponse(
                                                    fromIndex < total ? logs.subList(fromIndex, toIndex) : java.util.Collections.emptyList(),
                                                    (long) total,
                                                    page,
                                                    size
                                                );
                                            })
                                );
                    }
                });
    }

    /**
     * Get logs for a specific DAG, filtered by user role
     */
    public Flux<DagActionLogDTO> getLogsByDagId(String dagId) {
        return isAdmin()
                .flatMapMany(isAdmin -> {
                    if (isAdmin) {
                        // Admin can see all logs for the DAG
                        return dagActionLogRepository.findByDagIdOrderByTimestampDesc(dagId)
                                .map(this::mapToDTO);
                    } else {
                        // Non-admin users can only see their own logs for the DAG
                        return getCurrentUsername()
                                .flatMapMany(username -> 
                                    dagActionLogRepository.findByDagIdOrderByTimestampDesc(dagId)
                                            .filter(log -> log.getUsername().equals(username))
                                            .map(this::mapToDTO)
                                );
                    }
                });
    }

    /**
     * Get logs for a specific action type, filtered by user role
     */
    public Flux<DagActionLogDTO> getLogsByActionType(String actionType) {
        return isAdmin()
                .flatMapMany(isAdmin -> {
                    if (isAdmin) {
                        // Admin can see all logs of this type
                        return dagActionLogRepository.findByActionType(actionType)
                                .map(this::mapToDTO);
                    } else {
                        // Non-admin users can only see their own logs of this type
                        return getCurrentUsername()
                                .flatMapMany(username -> 
                                    dagActionLogRepository.findByActionType(actionType)
                                            .filter(log -> log.getUsername().equals(username))
                                            .map(this::mapToDTO)
                                );
                    }
                });
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