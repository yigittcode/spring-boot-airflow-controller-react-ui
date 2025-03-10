package com.yigit.airflow_spring_rest_controller.service;

import com.yigit.airflow_spring_rest_controller.dto.log.DagActionLogDTO;
import com.yigit.airflow_spring_rest_controller.dto.log.DagActionLogResponse;
import com.yigit.airflow_spring_rest_controller.entity.DagActionLog;
import com.yigit.airflow_spring_rest_controller.entity.DagActionLog.ActionType;
import com.yigit.airflow_spring_rest_controller.entity.Role;
import com.yigit.airflow_spring_rest_controller.repository.DagActionLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Service for managing DAG action logs
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DagActionLogService {

    private final DagActionLogRepository dagActionLogRepository;
    
    // ADMIN rolü tanımı - Spring Security, rolleri otomatik olarak "ROLE_" prefix'i ile kullanabilir
    // Bu nedenle isAdmin() metodunda her iki format da kontrol edilir
    private static final String ADMIN_ROLE = Role.ADMIN.name();
    private static final String UNKNOWN_USER = "unknown";

    /**
     * Logs a DAG action with the currently authenticated user
     * 
     * @param dagId The DAG identifier
     * @param actionType The type of action performed
     * @param actionDetails Details about the action
     * @param success Whether the action was successful
     * @param runId Optional run identifier
     * @return A Mono containing the created log entry
     */
    public Mono<DagActionLog> logDagAction(String dagId, ActionType actionType, String actionDetails, Boolean success, String runId) {
        log.info("Logging DAG action: {} for DAG: {}, details: {}, runId: {}", 
            actionType, dagId, actionDetails, runId != null ? runId : "N/A");
            
        return getCurrentUsername()
            .flatMap(username -> {
                LocalDateTime now = LocalDateTime.now();
                String actionTypeValue = actionType != null ? actionType.getValue() : ActionType.OTHER.getValue();
                
                // Ensure parameters are never null
                String safeUsername = username != null ? username : UNKNOWN_USER;
                String safeDagId = dagId != null ? dagId : "";
                String safeDetails = actionDetails != null ? actionDetails : "";
                Boolean safeSuccess = success != null ? success : false;
                
                // Use the repository's insertDagActionLog method and return the constructed log object
                return dagActionLogRepository.insertDagActionLog(
                        safeUsername, 
                        safeDagId, 
                        actionTypeValue, 
                        safeDetails,
                        now,
                        safeSuccess,
                        runId)
                    .then(Mono.just(DagActionLog.builder()
                        .username(safeUsername)
                        .dagId(safeDagId)
                        .actionType(actionTypeValue)
                        .actionDetails(safeDetails)
                        .timestamp(now)
                        .success(safeSuccess)
                        .runId(runId)
                        .build()))
                    .doOnSuccess(logEntry -> 
                        log.info("Successfully logged DAG action: {} for DAG: {}, user: {}", 
                            actionTypeValue, safeDagId, safeUsername));
            });
    }

    /**
     * Checks if the current user has admin privileges
     * 
     * @return A Mono containing a boolean indicating admin status
     */
    private Mono<Boolean> isAdmin() {
        return ReactiveSecurityContextHolder.getContext()
            .map(SecurityContext::getAuthentication)
            .filter(Objects::nonNull)
            .flatMap(auth -> {
                boolean isAdmin = auth.getAuthorities().stream()
                    .anyMatch(authority -> {
                        String authName = authority.getAuthority();
                        return authName.equals(ADMIN_ROLE) || authName.equals("ROLE_" + ADMIN_ROLE);
                    });
                
                log.debug("User '{}' admin status: {}", auth.getName(), isAdmin);
                return Mono.just(isAdmin);
            })
            .defaultIfEmpty(false);
    }

    /**
     * Gets the username of the currently authenticated user
     * 
     * @return A Mono containing the username
     */
    private Mono<String> getCurrentUsername() {
        return ReactiveSecurityContextHolder.getContext()
            .map(SecurityContext::getAuthentication)
            .filter(Objects::nonNull)
            .map(Authentication::getName)
            .doOnNext(username -> log.debug("Current username: {}", username))
            .defaultIfEmpty(UNKNOWN_USER);
    }

    /**
     * Retrieves all logs with pagination
     * 
     * @param page Page number (0-based)
     * @param size Page size
     * @return A Mono containing the response with logs and pagination info
     */
    public Mono<DagActionLogResponse> getAllLogs(int page, int size) {
        log.info("Retrieving all action logs, page: {}, size: {}", page, size);
        int offset = page * size;
        
        return isAdmin()
            .flatMap(isAdmin -> {
                if (isAdmin) {
                    log.debug("Admin user retrieving all logs");
                    return dagActionLogRepository.countAll()
                        .next()
                        .flatMap(total -> {
                            if (total == 0) {
                                log.debug("No logs found");
                                return Mono.just(new DagActionLogResponse(
                                    Collections.emptyList(), 0L, page, size));
                            }
                            
                            return dagActionLogRepository.findAllWithPagination(size, offset)
                                .map(this::mapToDTO)
                                .collectList()
                                .doOnSuccess(logs -> log.info("Retrieved {} logs for admin", logs.size()))
                                .map(logs -> new DagActionLogResponse(logs, total, page, size));
                        });
                } else {
                    // For non-admin users, get only their logs
                    return getCurrentUsername()
                        .flatMap(username -> 
                            dagActionLogRepository.findByUsername(username)
                                .map(this::mapToDTO)
                                .collectList()
                                .map(allLogs -> {
                                    long total = allLogs.size();
                                    List<DagActionLogDTO> pagedLogs;
                                    
                                    // Apply pagination manually
                                    if (offset < total) {
                                        int toIndex = Math.min(offset + size, allLogs.size());
                                        pagedLogs = allLogs.subList(offset, toIndex);
                                    } else {
                                        pagedLogs = new ArrayList<>();
                                    }
                                    
                                    log.info("Retrieved {} logs for user '{}'", pagedLogs.size(), username);
                                    return new DagActionLogResponse(pagedLogs, total, page, size);
                                })
                        );
                }
            });
    }

    /**
     * Retrieves logs for a specific DAG
     * 
     * @param dagId The DAG identifier
     * @return A Flux of log DTOs
     */
    public Flux<DagActionLogDTO> getLogsByDagId(String dagId) {
        log.info("Retrieving action logs for DAG: {}", dagId);
        return isAdmin()
            .flatMapMany(isAdmin -> {
                if (isAdmin) {
                    log.debug("Admin user retrieving logs for DAG: {}", dagId);
                    return dagActionLogRepository.findByDagIdOrderByTimestampDesc(dagId)
                        .map(this::mapToDTO)
                        .doOnComplete(() -> log.info("Completed retrieving logs for DAG: {}", dagId));
                } else {
                    return getCurrentUsername()
                        .flatMapMany(username -> {
                            log.debug("Non-admin user '{}' retrieving logs for DAG: {}", username, dagId);
                            return dagActionLogRepository.findByDagIdOrderByTimestampDesc(dagId)
                                .filter(log -> log.getUsername().equals(username))
                                .map(this::mapToDTO)
                                .doOnComplete(() -> log.info("Completed retrieving logs for DAG: {} and user: {}", 
                                    dagId, username));
                        });
                }
            });
    }

    /**
     * Retrieves logs for a specific action type
     * 
     * @param actionType The action type to filter by
     * @return A Flux of log DTOs
     */
    public Flux<DagActionLogDTO> getLogsByActionType(String actionType) {
        log.info("Retrieving action logs for action type: {}", actionType);
        return isAdmin()
            .flatMapMany(isAdmin -> {
                if (isAdmin) {
                    log.debug("Admin user retrieving logs for action type: {}", actionType);
                    return dagActionLogRepository.findByActionType(actionType)
                        .map(this::mapToDTO)
                        .doOnComplete(() -> log.info("Completed retrieving logs for action type: {}", actionType));
                } else {
                    return getCurrentUsername()
                        .flatMapMany(username -> {
                            log.debug("Non-admin user '{}' retrieving logs for action type: {}", username, actionType);
                            return dagActionLogRepository.findByActionType(actionType)
                                .filter(log -> log.getUsername().equals(username))
                                .map(this::mapToDTO)
                                .doOnComplete(() -> log.info("Completed retrieving logs for action type: {} and user: {}", 
                                    actionType, username));
                        });
                }
            });
    }

    /**
     * Maps a DagActionLog entity to a DTO
     * 
     * @param entity The entity to map
     * @return The mapped DTO
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