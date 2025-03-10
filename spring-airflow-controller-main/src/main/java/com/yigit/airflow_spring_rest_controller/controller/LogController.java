package com.yigit.airflow_spring_rest_controller.controller;

import com.yigit.airflow_spring_rest_controller.dto.log.DagActionLogDTO;
import com.yigit.airflow_spring_rest_controller.dto.log.DagActionLogResponse;
import com.yigit.airflow_spring_rest_controller.service.DagActionLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
@Tag(name = "Logs", description = "Operations for retrieving system logs, including DAG operation logs")
public class LogController {

    private final DagActionLogService dagActionLogService;

    @Operation(
        summary = "Get all DAG action logs",
        description = "Retrieves a paginated list of all DAG action logs in the system."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "DAG action logs successfully retrieved",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DagActionLogResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Authentication failed"
        )
    })
    @GetMapping("/dag-actions")
    public Mono<DagActionLogResponse> getAllDagActionLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return dagActionLogService.getAllLogs(page, size);
    }

    @Operation(
        summary = "Get DAG action logs by DAG ID",
        description = "Retrieves logs of actions performed on a specific DAG"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "DAG action logs successfully retrieved",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DagActionLogDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Authentication failed"
        )
    })
    @GetMapping("/dag-actions/dag/{dagId}")
    public Flux<DagActionLogDTO> getDagActionLogsByDagId(
            @Parameter(description = "The ID of the DAG", required = true)
            @PathVariable String dagId) {
        return dagActionLogService.getLogsByDagId(dagId);
    }

    @Operation(
        summary = "Get DAG action logs by action type",
        description = "Retrieves logs of actions of a specific type (e.g., PAUSED, TRIGGERED)"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "DAG action logs successfully retrieved",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DagActionLogDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Authentication failed"
        )
    })
    @GetMapping("/dag-actions/type/{actionType}")
    public Flux<DagActionLogDTO> getDagActionLogsByType(
            @Parameter(description = "The type of the action", required = true)
            @PathVariable String actionType) {
        return dagActionLogService.getLogsByActionType(actionType);
    }
} 