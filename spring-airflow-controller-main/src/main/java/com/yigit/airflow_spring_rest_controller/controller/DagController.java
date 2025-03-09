package com.yigit.airflow_spring_rest_controller.controller;

import com.yigit.airflow_spring_rest_controller.dto.dag.Dag;
import com.yigit.airflow_spring_rest_controller.dto.dag.DagCollection;
import com.yigit.airflow_spring_rest_controller.dto.dag.DagDetail;
import com.yigit.airflow_spring_rest_controller.dto.dag.DagUpdate;
import com.yigit.airflow_spring_rest_controller.dto.task.TaskCollection;
import com.yigit.airflow_spring_rest_controller.service.DagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.ArrayList;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/dags")
@Tag(name = "DAGs", description = "Operations for managing Apache Airflow DAGs including retrieving DAG details, " +
        "updating DAG configurations, and managing DAG states")
public class DagController {

    private final DagService dagService;

    @Autowired
    public DagController(DagService dagService) {
        this.dagService = dagService;
    }

    @Operation(
        summary = "Get all DAGs",
        description = "Retrieves a list of all DAGs in the Airflow environment. " +
                     "Returns basic information about each DAG including its ID, schedule interval, and current status."
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
        @ApiResponse(
            responseCode = "401",
            description = "Authentication failed"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Permission denied"
        )
    })
    @GetMapping
    public Mono<DagCollection> getDags(
        @RequestParam(required = false) Boolean isActive,
        @RequestParam(required = false) Boolean isPaused,
        @RequestParam(required = false) String search,
        @RequestParam(defaultValue = "0") int page,
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
                     "Includes information about the DAG's configuration, schedule, and current status."
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
        @ApiResponse(
            responseCode = "404",
            description = "DAG not found"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Authentication failed"
        )
    })
    @GetMapping("/{dagId}")
    public Mono<Dag> getDag(
        @Parameter(description = "The ID of the DAG to retrieve", 
                  required = true, 
                  example = "example_dag_id") 
        @PathVariable String dagId
    ) {
        return dagService.getDag(dagId);
    }

    @Operation(
        summary = "Update DAG configuration",
        description = "Updates the configuration of a specific DAG. " +
                     "Can be used to modify the DAG's pause status, tags, or other mutable properties. " +
                     "Note that some properties cannot be modified once a DAG is created."
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
        @ApiResponse(
            responseCode = "400",
            description = "Invalid update parameters provided"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "DAG not found"
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Update conflict - DAG might be in an invalid state for updates"
        )
    })
    @PatchMapping("/{dagId}")
    public Mono<Dag> updateDag(
        @Parameter(description = "The ID of the DAG to update", 
                  required = true, 
                  example = "example_dag_id") 
        @PathVariable String dagId,
        
        @Parameter(
            description = "Update parameters including pause status and other mutable properties",
            required = true,
            schema = @Schema(implementation = DagUpdate.class)
        )
        @RequestBody DagUpdate dagUpdate
    ) {
        return dagService.updateDag(dagId, dagUpdate);
    }

    @Operation(
        summary = "Delete a DAG",
        description = "Deletes a DAG from the Airflow environment. " +
                     "This operation will remove the DAG metadata from the database. " +
                     "Note: This does not delete the actual DAG file from the filesystem."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "204",
            description = "DAG successfully deleted"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "DAG not found"
        ),
        @ApiResponse(
            responseCode = "409",
            description = "DAG cannot be deleted (e.g., has running instances)"
        )
    })
    @DeleteMapping("/{dagId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteDag(
        @Parameter(description = "The ID of the DAG to delete", 
                  required = true, 
                  example = "example_dag_id") 
        @PathVariable String dagId
    ) {
        return dagService.deleteDag(dagId);
    }

    @Operation(
        summary = "Get DAG tasks",
        description = "Retrieves a list of all tasks defined in a specific DAG. " +
                     "Includes task configurations, dependencies, and other task-specific properties."
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
        @ApiResponse(
            responseCode = "404",
            description = "DAG not found"
        )
    })
    @GetMapping("/{dagId}/tasks")
    public Mono<TaskCollection> getDagTasks(
        @Parameter(description = "The ID of the DAG", 
                  required = true, 
                  example = "example_dag_id") 
        @PathVariable String dagId
    ) {
        return dagService.getDagTasks(dagId);
    }

    @Operation(
        summary = "Get DAG details",
        description = "Retrieves detailed information about a DAG including its source code, " +
                     "schedule interval, default arguments, and other configuration details."
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
        @ApiResponse(
            responseCode = "404",
            description = "DAG not found"
        )
    })
    @GetMapping("/{dagId}/details")
    public Mono<DagDetail> getDagDetails(
        @Parameter(description = "The ID of the DAG", 
                  required = true, 
                  example = "example_dag_id") 
        @PathVariable String dagId
    ) {
        return dagService.getDagDetails(dagId);
    }
} 