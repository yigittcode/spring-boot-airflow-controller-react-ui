package com.yigit.airflow_spring_rest_controller.controller;

import com.yigit.airflow_spring_rest_controller.dto.dagrun.DagRun;
import com.yigit.airflow_spring_rest_controller.dto.dagrun.DagRunCollection;
import com.yigit.airflow_spring_rest_controller.dto.dagrun.DagRunCreate;
import com.yigit.airflow_spring_rest_controller.dto.dagrun.DagRunStateUpdate;
import com.yigit.airflow_spring_rest_controller.dto.dagrun.DagRunClear;
import com.yigit.airflow_spring_rest_controller.dto.dagrun.DagRunNoteUpdate;
import com.yigit.airflow_spring_rest_controller.dto.dataset.DatasetEventCollection;
import com.yigit.airflow_spring_rest_controller.service.DagRunService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/v1/dags/{dagId}/dagRuns")
@Tag(name = "DAG Runs", description = "Operations for managing DAG Runs including creating, monitoring, " +
        "and controlling the execution of DAG instances")
public class DagRunController {

    private final DagRunService dagRunService;

    @Autowired
    public DagRunController(DagRunService dagRunService) {
        this.dagRunService = dagRunService;
    }

    @Operation(
        summary = "Get all DAG Runs",
        description = "Retrieves a list of all DAG Runs for a specific DAG. " +
                     "Returns execution history including run status, start time, and end time. " +
                     "Supports filtering by state and DAG Run ID."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "DAG Runs successfully retrieved",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DagRunCollection.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "DAG not found"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Authentication failed"
        )
    })
    @GetMapping
    public Mono<DagRunCollection> getDagRuns(
        @Parameter(description = "The ID of the DAG", 
                  required = true, 
                  example = "example_dag_id") 
        @PathVariable String dagId,
        
        @Parameter(description = "Filter by state", 
                   example = "running",
                   schema = @Schema(allowableValues = {"queued", "running", "success", "failed"}))
        @RequestParam(required = false) String state,
        
        @Parameter(description = "Filter by DAG Run ID", 
                   example = "manual_12345")
        @RequestParam(required = false, name = "dag_run_id") String dagRunId
    ) {
        Map<String, String> queryParams = new HashMap<>();
        if (state != null && !state.isEmpty()) {
            queryParams.put("state", state);
        }
        if (dagRunId != null && !dagRunId.isEmpty()) {
            queryParams.put("dag_run_id", dagRunId);
        }
        
        return dagRunService.getDagRuns(dagId, queryParams);
    }

    @Operation(
        summary = "Create a new DAG Run",
        description = "Triggers a new run of the specified DAG. " +
                     "Allows setting custom configuration and execution date. " +
                     "The DAG must be active (not paused) to create a new run."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "DAG Run successfully created",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DagRun.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request parameters"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "DAG not found"
        ),
        @ApiResponse(
            responseCode = "409",
            description = "DAG Run already exists for the specified date"
        )
    })
    @PostMapping
    public Mono<DagRun> createDagRun(
        @Parameter(description = "The ID of the DAG", 
                  required = true, 
                  example = "example_dag_id") 
        @PathVariable String dagId,
        
        @Parameter(
            description = "DAG Run creation parameters including execution date and configuration",
            required = true,
            schema = @Schema(implementation = DagRunCreate.class)
        )
        @RequestBody DagRunCreate dagRunCreate
    ) {
        return dagRunService.createDagRun(dagId, dagRunCreate);
    }

    @Operation(
        summary = "Get a specific DAG Run",
        description = "Retrieves detailed information about a specific DAG Run. " +
                     "Includes execution status, timing information, and configuration details."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "DAG Run details successfully retrieved",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DagRun.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "DAG or DAG Run not found"
        )
    })
    @GetMapping("/{dagRunId}")
    public Mono<DagRun> getDagRun(
        @Parameter(description = "The ID of the DAG", 
                  required = true, 
                  example = "example_dag_id") 
        @PathVariable String dagId,
        
        @Parameter(description = "The ID of the DAG Run", 
                  required = true, 
                  example = "scheduled__2024-02-24T10:00:00+00:00") 
        @PathVariable String dagRunId
    ) {
        return dagRunService.getDagRun(dagId, dagRunId);
    }

    @Operation(
        summary = "Delete a DAG Run",
        description = "Deletes a specific DAG Run and its associated task instances. " +
                     "This operation cannot be undone. Running instances will be terminated."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "204",
            description = "DAG Run successfully deleted"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "DAG or DAG Run not found"
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Cannot delete DAG Run in current state"
        )
    })
    @DeleteMapping("/{dagRunId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteDagRun(
        @Parameter(description = "The ID of the DAG", 
                  required = true, 
                  example = "example_dag_id") 
        @PathVariable String dagId,
        
        @Parameter(description = "The ID of the DAG Run to delete", 
                  required = true, 
                  example = "scheduled__2024-02-24T10:00:00+00:00") 
        @PathVariable String dagRunId
    ) {
        return dagRunService.deleteDagRun(dagId, dagRunId);
    }

    @Operation(
        summary = "Update DAG Run state",
        description = "Updates the state of a specific DAG Run. " +
                     "Can be used to mark a run as success, failed, or other valid states. " +
                     "Note that not all state transitions are allowed."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "DAG Run state successfully updated",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DagRun.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid state transition requested"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "DAG or DAG Run not found"
        )
    })
    @PatchMapping("/{dagRunId}")
    public Mono<DagRun> updateDagRunState(
        @Parameter(description = "The ID of the DAG", 
                  required = true, 
                  example = "example_dag_id") 
        @PathVariable String dagId,
        
        @Parameter(description = "The ID of the DAG Run", 
                  required = true, 
                  example = "scheduled__2024-02-24T10:00:00+00:00") 
        @PathVariable String dagRunId,
        
        @Parameter(
            description = "State update information",
            required = true,
            schema = @Schema(implementation = DagRunStateUpdate.class)
        )
        @RequestBody DagRunStateUpdate stateUpdate
    ) {
        return dagRunService.updateDagRunState(dagId, dagRunId, stateUpdate);
    }

    @Operation(
        summary = "Clear a DAG Run",
        description = "Clears the state of a DAG Run and its task instances. " +
                     "This resets the run's state and allows it to be re-run. " +
                     "Task instance history will be preserved."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "DAG Run successfully cleared",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DagRun.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "DAG or DAG Run not found"
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Cannot clear DAG Run in current state"
        )
    })
    @PostMapping("/{dagRunId}/clear")
    public Mono<DagRun> clearDagRun(
        @Parameter(description = "The ID of the DAG", 
                  required = true, 
                  example = "example_dag_id") 
        @PathVariable String dagId,
        
        @Parameter(description = "The ID of the DAG Run", 
                  required = true, 
                  example = "scheduled__2024-02-24T10:00:00+00:00") 
        @PathVariable String dagRunId,
        
        @Parameter(
            description = "Clear operation parameters",
            required = true,
            schema = @Schema(implementation = DagRunClear.class)
        )
        @RequestBody DagRunClear clearRequest
    ) {
        return dagRunService.clearDagRun(dagId, dagRunId, clearRequest);
    }

    @Operation(
        summary = "Get upstream dataset events",
        description = "Retrieves the dataset events that triggered this DAG Run. " +
                     "Only applicable for dataset-triggered DAGs. " +
                     "Returns empty if the DAG is not dataset-dependent."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Dataset events successfully retrieved",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DatasetEventCollection.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "DAG or DAG Run not found"
        )
    })
    @GetMapping("/{dagRunId}/upstreamDatasetEvents")
    public Mono<DatasetEventCollection> getUpstreamDatasetEvents(
        @Parameter(description = "The ID of the DAG", 
                  required = true, 
                  example = "example_dag_id") 
        @PathVariable String dagId,
        
        @Parameter(description = "The ID of the DAG Run", 
                  required = true, 
                  example = "scheduled__2024-02-24T10:00:00+00:00") 
        @PathVariable String dagRunId
    ) {
        return dagRunService.getUpstreamDatasetEvents(dagId, dagRunId);
    }

    @PatchMapping("/{dagRunId}/setNote")
    public Mono<DagRun> setDagRunNote(
        @PathVariable String dagId,
        @PathVariable String dagRunId,
        @RequestBody DagRunNoteUpdate noteUpdate
    ) {
        return dagRunService.setDagRunNote(dagId, dagRunId, noteUpdate);
    }
} 