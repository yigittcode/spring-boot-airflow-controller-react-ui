package com.yigit.airflow_spring_rest_controller.controller;

import com.yigit.airflow_spring_rest_controller.service.LogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/logs")
@Tag(name = "Logs", description = "Operations related to task logs")
public class LogController {

    private final LogService logService;

    @Autowired
    public LogController(LogService logService) {
        this.logService = logService;
    }

    @Operation(
        summary = "Get Task Logs",
        description = "Retrieve logs for a specific task instance"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Logs retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Task instance not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{dagId}/dagRuns/{dagRunId}/taskInstances/{taskId}")
    public Mono<String> getTaskLogs(
        @Parameter(description = "The DAG ID") @PathVariable String dagId,
        @Parameter(description = "The DAG run ID") @PathVariable String dagRunId,
        @Parameter(description = "The task ID") @PathVariable String taskId,
        @Parameter(description = "The task try number") @RequestParam(required = false, defaultValue = "1") Integer tryNumber
    ) {
        return logService.getTaskLogs(dagId, dagRunId, taskId, tryNumber);
    }
} 