package com.yigit.airflow_spring_rest_controller.service;

import com.yigit.airflow_spring_rest_controller.dto.dag.Dag;
import com.yigit.airflow_spring_rest_controller.dto.dag.DagCollection;
import com.yigit.airflow_spring_rest_controller.dto.dag.DagDetail;
import com.yigit.airflow_spring_rest_controller.dto.dag.DagUpdate;
import com.yigit.airflow_spring_rest_controller.dto.task.TaskCollection;
import com.yigit.airflow_spring_rest_controller.entity.DagActionLog.ActionType;
import com.yigit.airflow_spring_rest_controller.util.WebClientUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Map;

/**
 * Service for interacting with Airflow DAGs
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DagService {

    private final WebClient airflowWebClient;
    private final DagActionLogService dagActionLogService;
    
    private static final String DAG_BASE_PATH = "/dags";
    private static final String DAG_RESOURCE_NAME = "DAG";
    
    /**
     * Retrieves a collection of all DAGs
     * 
     * @return A Mono containing all DAGs
     */
    public Mono<DagCollection> getDags() {
        log.info("Retrieving all DAGs");
        return WebClientUtil.get(
            airflowWebClient, 
            DAG_BASE_PATH, 
            Collections.emptyMap(), 
            null, 
            DagCollection.class,
            DAG_RESOURCE_NAME
        ).doOnSuccess(result -> log.info("Successfully retrieved {} DAGs", 
            result.getDags() != null ? result.getDags().size() : 0));
    }

    /**
     * Retrieves a specific DAG by ID
     * 
     * @param dagId The DAG identifier
     * @return A Mono containing the requested DAG
     */
    public Mono<Dag> getDag(String dagId) {
        log.info("Retrieving DAG with ID: {}", dagId);
        Map<String, Object> pathVars = Collections.singletonMap("dagId", dagId);
        return WebClientUtil.get(
            airflowWebClient, 
            DAG_BASE_PATH + "/{dagId}", 
            pathVars, 
            null, 
            Dag.class,
            DAG_RESOURCE_NAME
        ).doOnSuccess(dag -> log.info("Successfully retrieved DAG: {}", dagId));
    }

    /**
     * Updates a DAG with the provided configuration
     * 
     * @param dagId The DAG identifier
     * @param dagUpdate The update configuration
     * @return A Mono containing the updated DAG
     */
    public Mono<Dag> updateDag(String dagId, DagUpdate dagUpdate) {
        log.info("Updating DAG with ID: {}, update: {}", dagId, dagUpdate);
        Map<String, Object> pathVars = Collections.singletonMap("dagId", dagId);
        return WebClientUtil.patch(
            airflowWebClient, 
            DAG_BASE_PATH + "/{dagId}", 
            pathVars, 
            dagUpdate, 
            Dag.class,
            DAG_RESOURCE_NAME
        ).flatMap(dag -> {
            log.info("Successfully updated DAG: {}", dagId);
            return logDagAction(dagId, dag, dagUpdate);
        });
    }
    
    /**
     * Log DAG actions based on the update performed
     */
    private Mono<Dag> logDagAction(String dagId, Dag dag, DagUpdate dagUpdate) {
        String actionDetails = "DAG updated";
        ActionType actionType = ActionType.OTHER;
        
        if (dagUpdate.getIsPaused() != null) {
            actionType = dagUpdate.getIsPaused() ? ActionType.PAUSED : ActionType.UNPAUSED;
            actionDetails = dagUpdate.getIsPaused() ? "DAG paused" : "DAG unpaused";
            log.info("DAG {} {}", dagId, actionDetails.toLowerCase());
        }
        
        return dagActionLogService.logDagAction(dagId, actionType, actionDetails, true, null)
            .thenReturn(dag);
    }

    /**
     * Deletes a specific DAG
     * 
     * @param dagId The DAG identifier
     * @return A Mono that completes when the DAG is deleted
     */
    public Mono<Void> deleteDag(String dagId) {
        log.info("Deleting DAG with ID: {}", dagId);
        Map<String, Object> pathVars = Collections.singletonMap("dagId", dagId);
        return WebClientUtil.delete(
            airflowWebClient, 
            DAG_BASE_PATH + "/{dagId}", 
            pathVars, 
            DAG_RESOURCE_NAME
        ).then(
            dagActionLogService.logDagAction(dagId, ActionType.DELETED, "DAG deleted", true, null)
                .then(Mono.fromRunnable(() -> log.info("Successfully deleted DAG: {}", dagId)))
        );
    }

    /**
     * Retrieves tasks for a specific DAG
     * 
     * @param dagId The DAG identifier
     * @return A Mono containing the DAG's tasks
     */
    public Mono<TaskCollection> getDagTasks(String dagId) {
        log.info("Retrieving tasks for DAG with ID: {}", dagId);
        Map<String, Object> pathVars = Collections.singletonMap("dagId", dagId);
        return WebClientUtil.get(
            airflowWebClient, 
            DAG_BASE_PATH + "/{dagId}/tasks", 
            pathVars, 
            null, 
            TaskCollection.class,
            DAG_RESOURCE_NAME
        ).doOnSuccess(tasks -> log.info("Successfully retrieved tasks for DAG: {}, count: {}", 
            dagId, tasks.getTasks() != null ? tasks.getTasks().size() : 0));
    }

    /**
     * Retrieves detailed information about a specific DAG
     * 
     * @param dagId The DAG identifier
     * @return A Mono containing detailed DAG information
     */
    public Mono<DagDetail> getDagDetails(String dagId) {
        log.info("Retrieving details for DAG with ID: {}", dagId);
        Map<String, Object> pathVars = Collections.singletonMap("dagId", dagId);
        return WebClientUtil.get(
            airflowWebClient, 
            DAG_BASE_PATH + "/{dagId}/details", 
            pathVars, 
            null, 
            DagDetail.class,
            DAG_RESOURCE_NAME
        ).doOnSuccess(details -> log.info("Successfully retrieved details for DAG: {}", dagId));
    }
} 