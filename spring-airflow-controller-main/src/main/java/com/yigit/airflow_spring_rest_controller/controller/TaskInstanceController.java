package com.yigit.airflow_spring_rest_controller.controller;

import com.yigit.airflow_spring_rest_controller.dto.task.TaskInstance;
import com.yigit.airflow_spring_rest_controller.dto.task.TaskInstanceCollection;
import com.yigit.airflow_spring_rest_controller.service.TaskInstanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.HashMap;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/dags/{dagId}/dagRuns/{dagRunId}/taskInstances")
@Tag(name = "Task Instances", description = "Operations for viewing Airflow Task Instances")
public class TaskInstanceController {

    private final TaskInstanceService taskInstanceService;

    @Autowired
    public TaskInstanceController(TaskInstanceService taskInstanceService) {
        this.taskInstanceService = taskInstanceService;
    }

    @Operation(
        summary = "Get a specific task instance",
        description = "Retrieves detailed information about a specific task instance within a DAG run. " +
                     "This includes the task's current state, start time, end time, and other execution details."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Task instance details successfully retrieved",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = TaskInstance.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Task instance not found or DAG/DAG Run does not exist"
        )
    })
    @GetMapping("/{taskId}")
    public Mono<TaskInstance> getTaskInstance(
        @Parameter(description = "The ID of the DAG", required = true, example = "example_dag_id") 
        @PathVariable String dagId,
        
        @Parameter(description = "The ID of the DAG Run", required = true, 
                  example = "scheduled__2024-02-24T10:00:00+00:00") 
        @PathVariable String dagRunId,
        
        @Parameter(description = "The ID of the Task", required = true, example = "task_1") 
        @PathVariable String taskId
    ) {
        return taskInstanceService.getTaskInstance(dagId, dagRunId, taskId);
    }

    @Operation(
        summary = "Get all task instances",
        description = "Retrieves a list of all task instances for a specific DAG Run. " +
                     "Supports filtering by various parameters including state, start date, and end date."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Task instances retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = TaskInstanceCollection.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "DAG or DAG Run not found"
        )
    })
    @GetMapping
    public Mono<TaskInstanceCollection> getTaskInstances(
        @PathVariable String dagId,
        @PathVariable String dagRunId,
        @RequestParam(required = false) Map<String, String> params
    ) {
        Map<String, List<String>> queryParams = new HashMap<>();
        if (params != null) {
            params.forEach((key, value) -> queryParams.put(key, Collections.singletonList(value)));
        }
        return taskInstanceService.getTaskInstances(dagId, dagRunId, queryParams);
    }
} 