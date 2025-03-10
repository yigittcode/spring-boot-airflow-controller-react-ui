package com.yigit.airflow_spring_rest_controller.controller;

import com.yigit.airflow_spring_rest_controller.dto.dagrun.DagRun;
import com.yigit.airflow_spring_rest_controller.dto.dagrun.DagRunCollection;
import com.yigit.airflow_spring_rest_controller.dto.dagrun.DagRunCreate;
import com.yigit.airflow_spring_rest_controller.dto.dagrun.DagRunStateUpdate;
import com.yigit.airflow_spring_rest_controller.dto.dagrun.DagRunClear;
import com.yigit.airflow_spring_rest_controller.dto.dagrun.DagRunNoteUpdate;
import com.yigit.airflow_spring_rest_controller.dto.dataset.DatasetEventCollection;
import com.yigit.airflow_spring_rest_controller.service.DagRunService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
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

import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("${api.endpoint.prefix}/dags/{dagId}/dagRuns")
@Tag(
    name = "DAG Runs", 
    description = "Operations for managing DAG execution runs, including triggering new runs, " +
                "monitoring run status, clearing failed tasks, and managing run metadata. " +
                "DAG Runs represent specific executions of DAGs and progress through states: " +
                "queued → running → success/failed. These endpoints allow complete management " +
                "of the DAG Run lifecycle."
)
@RequiredArgsConstructor
public class DagRunController {

    private final DagRunService dagRunService;

    @Operation(
        summary = "Get all DAG Runs",
        description = "Retrieves a list of all DAG Runs for a specific DAG. " +
                     "Returns execution history including run status, start time, and end time. " +
                     "Supports filtering by state and DAG Run ID. This endpoint is useful for " +
                     "monitoring the execution history of a DAG and investigating past runs."
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
        @ApiResponse(responseCode = "404", description = "DAG not found - The specified DAG ID does not exist"),
        @ApiResponse(responseCode = "401", description = "Authentication failed - Valid credentials required")
    })
    @GetMapping
    public Mono<DagRunCollection> getDagRuns(
        @Parameter(
            description = "The ID of the DAG to retrieve runs for - must match an existing DAG in Airflow", 
            required = true, 
            example = "example_dag_id"
        ) 
        @PathVariable String dagId,
        
        @Parameter(
            description = "Filter by state of the DAG run", 
            example = "running",
            schema = @Schema(allowableValues = {"queued", "running", "success", "failed"})
        )
        @RequestParam(required = false) String state,
        
        @Parameter(
            description = "Filter by DAG Run ID for finding a specific run", 
            example = "manual_2023-01-15T14:30:00+00:00"
        )
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
                     "This can be used to manually execute a DAG outside of its scheduled intervals. " +
                     "You can optionally provide configuration parameters, a specific execution date, " +
                     "and notes. This is one of the most commonly used endpoints for triggering workflows."
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
        @ApiResponse(responseCode = "400", description = "Invalid request parameters - The request contains invalid values"),
        @ApiResponse(responseCode = "404", description = "DAG not found - The specified DAG ID does not exist"),
        @ApiResponse(responseCode = "409", description = "Conflict - A DAG Run with the same run_id already exists")
    })
    @PostMapping
    public Mono<DagRun> createDagRun(
        @Parameter(
            description = "The ID of the DAG to trigger - must match an existing DAG in Airflow", 
            required = true, 
            example = "example_dag_id"
        ) 
        @PathVariable String dagId,
        
        @Parameter(
            description = "The configuration for creating a new DAG run, including execution date, parameters and notes", 
            required = true,
            content = @Content(
                examples = {
                    @ExampleObject(
                        name = "Simple trigger",
                        value = "{\"dag_run_id\": \"manual_trigger_001\", \"note\": \"Triggered manually\"}",
                        description = "Basic example to trigger a DAG run with a custom ID and note"
                    ),
                    @ExampleObject(
                        name = "With configuration parameters",
                        value = "{\"dag_run_id\": \"manual_trigger_002\", \"conf\": {\"param1\": \"value1\", \"threshold\": 95}, \"note\": \"Triggered with custom parameters\"}",
                        description = "Trigger a DAG run with configuration parameters passed to the DAG"
                    )
                }
            )
        )
        @RequestBody DagRunCreate dagRunCreate
    ) {
        return dagRunService.createDagRun(dagId, dagRunCreate);
    }

    @Operation(
        summary = "Get a specific DAG Run",
        description = "Retrieves details about a specific DAG Run using its ID. " +
                     "This provides comprehensive information about the run's status, " +
                     "start time, end time, and other metadata. Useful for checking " +
                     "the status of a specific execution or investigating issues."
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
        @ApiResponse(responseCode = "404", description = "DAG Run not found - The specified DAG Run ID does not exist for this DAG")
    })
    @GetMapping("/{dagRunId}")
    public Mono<DagRun> getDagRun(
        @Parameter(
            description = "The ID of the DAG - must match an existing DAG in Airflow", 
            required = true, 
            example = "example_dag_id"
        ) 
        @PathVariable String dagId,
        
        @Parameter(
            description = "The ID of the DAG Run to retrieve - must match an existing run for the specified DAG", 
            required = true, 
            example = "manual_2023-01-15T14:30:00+00:00"
        ) 
        @PathVariable String dagRunId
    ) {
        return dagRunService.getDagRun(dagId, dagRunId);
    }

    @Operation(
        summary = "Delete a DAG Run",
        description = "Deletes a specific DAG Run. This removes the run and its metadata from the system. " +
                     "Note that this operation may be restricted based on the state of the run " +
                     "(e.g., running runs typically cannot be deleted). Use with caution as it " +
                     "permanently removes execution history."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "DAG Run successfully deleted - No content returned"),
        @ApiResponse(responseCode = "404", description = "DAG Run not found - The specified DAG Run ID does not exist for this DAG"),
        @ApiResponse(responseCode = "409", description = "Conflict - The DAG Run cannot be deleted in its current state")
    })
    @DeleteMapping("/{dagRunId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteDagRun(
        @Parameter(
            description = "The ID of the DAG - must match an existing DAG in Airflow", 
            required = true, 
            example = "example_dag_id"
        ) 
        @PathVariable String dagId,
        
        @Parameter(
            description = "The ID of the DAG Run to delete - must match an existing run for the specified DAG", 
            required = true, 
            example = "manual_2023-01-15T14:30:00+00:00"
        ) 
        @PathVariable String dagRunId
    ) {
        return dagRunService.deleteDagRun(dagId, dagRunId);
    }

    @Operation(
        summary = "Update DAG Run state",
        description = "Updates the state of a specific DAG Run. This can be used to mark a run as " +
                     "success or failed, which can be useful for automated testing or handling " +
                     "partially completed runs. Note that state transitions may be restricted " +
                     "based on the current state of the run and Airflow's state transition rules."
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
        @ApiResponse(responseCode = "400", description = "Invalid state transition - The requested state change is not allowed"),
        @ApiResponse(responseCode = "404", description = "DAG Run not found - The specified DAG Run ID does not exist for this DAG")
    })
    @PatchMapping("/{dagRunId}")
    public Mono<DagRun> updateDagRunState(
        @Parameter(
            description = "The ID of the DAG - must match an existing DAG in Airflow", 
            required = true, 
            example = "example_dag_id"
        ) 
        @PathVariable String dagId,
        
        @Parameter(
            description = "The ID of the DAG Run to update - must match an existing run for the specified DAG", 
            required = true, 
            example = "manual_2023-01-15T14:30:00+00:00"
        ) 
        @PathVariable String dagRunId,
        
        @Parameter(
            description = "The state update information", 
            required = true,
            content = @Content(
                examples = {
                    @ExampleObject(
                        name = "Mark as success",
                        value = "{\"state\": \"success\"}",
                        description = "Mark the DAG Run as successful"
                    ),
                    @ExampleObject(
                        name = "Mark as failed",
                        value = "{\"state\": \"failed\"}",
                        description = "Mark the DAG Run as failed"
                    )
                }
            )
        )
        @RequestBody DagRunStateUpdate stateUpdate
    ) {
        return dagRunService.updateDagRunState(dagId, dagRunId, stateUpdate);
    }

    @Operation(
        summary = "Clear a DAG Run",
        description = "Clears the state of tasks in a DAG Run, allowing them to be re-run. " +
                     "This is particularly useful for recovering from failed tasks or when " +
                     "tasks need to be re-executed with updated code or configuration. " +
                     "Can target specific tasks or the entire DAG Run."
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
        @ApiResponse(responseCode = "404", description = "DAG Run not found - The specified DAG Run ID does not exist for this DAG"),
        @ApiResponse(responseCode = "409", description = "Conflict - The DAG Run cannot be cleared in its current state")
    })
    @PostMapping("/{dagRunId}/clear")
    public Mono<DagRun> clearDagRun(
        @Parameter(
            description = "The ID of the DAG - must match an existing DAG in Airflow", 
            required = true, 
            example = "example_dag_id"
        ) 
        @PathVariable String dagId,
        
        @Parameter(
            description = "The ID of the DAG Run to clear - must match an existing run for the specified DAG", 
            required = true, 
            example = "manual_2023-01-15T14:30:00+00:00"
        ) 
        @PathVariable String dagRunId,
        
        @Parameter(
            description = "Clear options, including task IDs to clear and whether to reset downstream tasks", 
            required = true,
            content = @Content(
                examples = {
                    @ExampleObject(
                        name = "Clear all tasks",
                        value = "{\"dry_run\": false, \"reset_dag_runs\": true}",
                        description = "Clear all tasks in the DAG Run"
                    ),
                    @ExampleObject(
                        name = "Clear specific tasks",
                        value = "{\"task_ids\": [\"task1\", \"task2\"], \"dry_run\": false, \"reset_dag_runs\": true}",
                        description = "Clear only the specified tasks"
                    )
                }
            )
        )
        @RequestBody DagRunClear clearRequest
    ) {
        return dagRunService.clearDagRun(dagId, dagRunId, clearRequest);
    }

    @Operation(
        summary = "Get upstream dataset events",
        description = "Retrieves the dataset events that triggered this DAG Run. " +
                     "This is specifically useful for data-driven DAGs that are triggered by " +
                     "dataset updates. The response includes information about which datasets " +
                     "were updated and the timing of those updates."
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
        @ApiResponse(responseCode = "404", description = "DAG Run not found - The specified DAG Run ID does not exist for this DAG")
    })
    @GetMapping("/{dagRunId}/upstreamDatasetEvents")
    public Mono<DatasetEventCollection> getUpstreamDatasetEvents(
        @Parameter(
            description = "The ID of the DAG - must match an existing DAG in Airflow", 
            required = true, 
            example = "example_dag_id"
        ) 
        @PathVariable String dagId,
        
        @Parameter(
            description = "The ID of the DAG Run to get events for - must match an existing run for the specified DAG", 
            required = true, 
            example = "scheduled__2023-01-15T14:30:00+00:00"
        ) 
        @PathVariable String dagRunId
    ) {
        return dagRunService.getUpstreamDatasetEvents(dagId, dagRunId);
    }

    @Operation(
        summary = "Set a note for a DAG Run",
        description = "Sets or updates a note associated with a specific DAG Run. " +
                     "Notes can be used to provide context, document reasons for manual triggers, " +
                     "record investigation findings, or add any relevant information about the run. " +
                     "This is particularly useful for audit trails and team collaboration."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Note successfully updated",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DagRun.class)
            )
        ),
        @ApiResponse(responseCode = "404", description = "DAG Run not found - The specified DAG Run ID does not exist for this DAG")
    })
    @PatchMapping("/{dagRunId}/setNote")
    public Mono<DagRun> setDagRunNote(
        @Parameter(
            description = "The ID of the DAG - must match an existing DAG in Airflow", 
            required = true, 
            example = "example_dag_id"
        ) 
        @PathVariable String dagId,
        
        @Parameter(
            description = "The ID of the DAG Run to update - must match an existing run for the specified DAG", 
            required = true, 
            example = "manual_2023-01-15T14:30:00+00:00"
        ) 
        @PathVariable String dagRunId,
        
        @Parameter(
            description = "The updated note content", 
            required = true,
            content = @Content(
                examples = {
                    @ExampleObject(
                        name = "Add note",
                        value = "{\"note\": \"This run was manually triggered to process backlogged data\"}",
                        description = "Add a descriptive note to the DAG Run"
                    )
                }
            )
        )
        @RequestBody DagRunNoteUpdate noteUpdate
    ) {
        return dagRunService.setDagRunNote(dagId, dagRunId, noteUpdate);
    }
} 