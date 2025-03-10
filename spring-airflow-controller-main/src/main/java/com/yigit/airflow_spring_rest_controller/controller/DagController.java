package com.yigit.airflow_spring_rest_controller.controller;

import com.yigit.airflow_spring_rest_controller.dto.dag.Dag;
import com.yigit.airflow_spring_rest_controller.dto.dag.DagCollection;
import com.yigit.airflow_spring_rest_controller.dto.dag.DagDetail;
import com.yigit.airflow_spring_rest_controller.dto.dag.DagUpdate;
import com.yigit.airflow_spring_rest_controller.dto.task.TaskCollection;
import com.yigit.airflow_spring_rest_controller.service.DagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("${api.endpoint.prefix}/dags")
@Tag(
    name = "DAGs", 
    description = "Operations for managing Apache Airflow DAGs, including retrieving DAG details, " +
                 "updating DAG configurations, pausing/unpausing DAGs, and managing DAG states. " +
                 "These endpoints interface with the Airflow API to provide centralized DAG management."
)
@RequiredArgsConstructor
public class DagController {

    private final DagService dagService;

    @Operation(
        summary = "Get all DAGs",
        description = "Retrieves a paginated list of all DAGs in the Airflow environment. " +
                     "Returns basic information about each DAG including its ID, schedule interval, current status, " +
                     "and other configuration properties. Supports filtering by active status, paused status, " +
                     "and search text."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200", 
            description = "List of DAGs successfully retrieved",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DagCollection.class)
            )
        ),
        @ApiResponse(responseCode = "401", description = "Authentication failed - Valid credentials required"),
        @ApiResponse(responseCode = "403", description = "Permission denied - User lacks required permissions")
    })
    @GetMapping
    public Mono<DagCollection> getDags(
        @Parameter(
            description = "Filter by active status - when true, returns only active DAGs; when false, returns only inactive DAGs", 
            example = "true"
        )
        @RequestParam(required = false) Boolean isActive,
        
        @Parameter(
            description = "Filter by paused status - when true, returns only paused DAGs; when false, returns only unpaused DAGs", 
            example = "false"
        )
        @RequestParam(required = false) Boolean isPaused,
        
        @Parameter(
            description = "Search term to filter DAGs by ID or description (case-insensitive partial match)", 
            example = "example_flow"
        )
        @RequestParam(required = false) String search,
        
        @Parameter(
            description = "Page number for pagination (0-based)", 
            example = "0"
        )
        @RequestParam(defaultValue = "0") int page,
        
        @Parameter(
            description = "Number of items per page", 
            example = "10"
        )
        @RequestParam(defaultValue = "10") int size
    ) {
        return dagService.getDags()
            .map(dagCollection -> {
                List<Dag> filteredDags = dagCollection.getDags().stream()
                    .filter(dag -> isActive == null || dag.getIsActive() == isActive)
                    .filter(dag -> isPaused == null || dag.getIsPaused() == isPaused)
                    .filter(dag -> search == null || 
                        dag.getDagId().toLowerCase().contains(search.toLowerCase()) || 
                        (dag.getDescription() != null && dag.getDescription().toLowerCase().contains(search.toLowerCase())))
                    .collect(Collectors.toList());

                int totalElements = filteredDags.size();
                int fromIndex = page * size;
                int toIndex = Math.min(fromIndex + size, totalElements);

                DagCollection paginatedCollection = new DagCollection();
                paginatedCollection.setDags(fromIndex < totalElements ? 
                    filteredDags.subList(fromIndex, toIndex) : 
                    new ArrayList<>());
                paginatedCollection.setTotalEntries(totalElements);
                return paginatedCollection;
            });
    }

    @Operation(
        summary = "Get a specific DAG",
        description = "Retrieves detailed information about a specific DAG by its ID. " +
                     "Includes information about the DAG's configuration, schedule interval, current status, " +
                     "and other properties. This endpoint provides essential metadata about the DAG's current state."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200", 
            description = "DAG details successfully retrieved",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Dag.class)
            )
        ),
        @ApiResponse(responseCode = "404", description = "DAG not found - The specified DAG ID does not exist"),
        @ApiResponse(responseCode = "401", description = "Authentication failed - Valid credentials required")
    })
    @GetMapping("/{dagId}")
    public Mono<Dag> getDag(
        @Parameter(
            description = "The ID of the DAG to retrieve - must match an existing DAG in Airflow", 
            required = true, 
            example = "example_dag_id"
        ) 
        @PathVariable String dagId
    ) {
        return dagService.getDag(dagId);
    }

    @Operation(
        summary = "Update DAG configuration",
        description = "Updates the configuration of a specific DAG. " +
                     "Can be used to modify the DAG's pause status, tags, or other mutable properties. " +
                     "Note that some properties cannot be modified once a DAG is created. " +
                     "This endpoint is commonly used to pause or unpause a DAG."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200", 
            description = "DAG configuration successfully updated",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Dag.class)
            )
        ),
        @ApiResponse(responseCode = "400", description = "Invalid update parameters - The request contains invalid or incompatible values"),
        @ApiResponse(responseCode = "404", description = "DAG not found - The specified DAG ID does not exist"),
        @ApiResponse(responseCode = "409", description = "Update conflict - DAG might be in an invalid state for updates")
    })
    @PatchMapping("/{dagId}")
    public Mono<Dag> updateDag(
        @Parameter(
            description = "The ID of the DAG to update - must match an existing DAG in Airflow", 
            required = true, 
            example = "example_dag_id"
        ) 
        @PathVariable String dagId,
        
        @Parameter(
            description = "Update parameters containing fields to modify", 
            required = true,
            schema = @Schema(implementation = DagUpdate.class),
            content = @Content(
                examples = {
                    @ExampleObject(
                        name = "Pause DAG",
                        value = "{\"is_paused\": true}",
                        description = "Example to pause a DAG"
                    ),
                    @ExampleObject(
                        name = "Unpause DAG",
                        value = "{\"is_paused\": false}",
                        description = "Example to unpause a DAG"
                    )
                }
            )
        )
        @RequestBody DagUpdate dagUpdate
    ) {
        return dagService.updateDag(dagId, dagUpdate);
    }

    @Operation(
        summary = "Delete a DAG",
        description = "Deletes a DAG from the Airflow environment. " +
                     "This operation will remove the DAG metadata from the database. " +
                     "Note: This does not delete the actual DAG file from the filesystem. " +
                     "Use with caution as it may affect historical records associated with the DAG."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "DAG successfully deleted - No content returned"),
        @ApiResponse(responseCode = "404", description = "DAG not found - The specified DAG ID does not exist"),
        @ApiResponse(responseCode = "409", description = "DAG cannot be deleted - The DAG may have active runs or other dependencies")
    })
    @DeleteMapping("/{dagId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteDag(
        @Parameter(
            description = "The ID of the DAG to delete - must match an existing DAG in Airflow", 
            required = true, 
            example = "example_dag_id"
        ) 
        @PathVariable String dagId
    ) {
        return dagService.deleteDag(dagId);
    }

    @Operation(
        summary = "Get DAG tasks",
        description = "Retrieves a list of all tasks defined in a specific DAG. " +
                     "Includes task configurations, dependencies, and other task-specific properties. " +
                     "This provides a comprehensive view of the DAG structure and workflow components."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200", 
            description = "DAG tasks successfully retrieved",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = TaskCollection.class)
            )
        ),
        @ApiResponse(responseCode = "404", description = "DAG not found - The specified DAG ID does not exist")
    })
    @GetMapping("/{dagId}/tasks")
    public Mono<TaskCollection> getDagTasks(
        @Parameter(
            description = "The ID of the DAG to retrieve tasks for - must match an existing DAG in Airflow", 
            required = true, 
            example = "example_dag_id"
        ) 
        @PathVariable String dagId
    ) {
        return dagService.getDagTasks(dagId);
    }

    @Operation(
        summary = "Get DAG details",
        description = "Retrieves detailed information about a DAG including its source code, " +
                     "schedule interval, default arguments, and other configuration details. " +
                     "This endpoint provides the most comprehensive view of the DAG definition and configuration."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200", 
            description = "DAG details successfully retrieved",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DagDetail.class)
            )
        ),
        @ApiResponse(responseCode = "404", description = "DAG not found - The specified DAG ID does not exist")
    })
    @GetMapping("/{dagId}/details")
    public Mono<DagDetail> getDagDetails(
        @Parameter(
            description = "The ID of the DAG to retrieve details for - must match an existing DAG in Airflow", 
            required = true, 
            example = "example_dag_id"
        ) 
        @PathVariable String dagId
    ) {
        return dagService.getDagDetails(dagId);
    }
} 