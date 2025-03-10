package com.yigit.airflow_spring_rest_controller.service;

import com.yigit.airflow_spring_rest_controller.dto.dagrun.DagRun;
import com.yigit.airflow_spring_rest_controller.dto.dagrun.DagRunCollection;
import com.yigit.airflow_spring_rest_controller.dto.dagrun.DagRunCreate;
import com.yigit.airflow_spring_rest_controller.dto.dagrun.DagRunStateUpdate;
import com.yigit.airflow_spring_rest_controller.dto.dagrun.DagRunClear;
import com.yigit.airflow_spring_rest_controller.dto.dagrun.DagRunNoteUpdate;
import com.yigit.airflow_spring_rest_controller.dto.dataset.DatasetEventCollection;
import com.yigit.airflow_spring_rest_controller.entity.DagActionLog.ActionType;
import com.yigit.airflow_spring_rest_controller.util.WebClientUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for interacting with Airflow DAG Runs
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DagRunService {

    private final WebClient airflowWebClient;
    private final DagActionLogService dagActionLogService;
    
    private static final String DAG_BASE_PATH = "/dags/{dagId}/dagRuns";
    private static final String DAG_RUN_RESOURCE_NAME = "DAG Run";
    
    /**
     * Retrieves a collection of DAG Runs for a specific DAG
     * 
     * @param dagId The DAG identifier
     * @param queryParams Optional query parameters for filtering
     * @return A Mono containing the DAG Runs
     */
    public Mono<DagRunCollection> getDagRuns(String dagId, Map<String, String> queryParams) {
        log.info("Retrieving DAG runs for DAG: {}, with filters: {}", dagId, queryParams);
        Map<String, Object> pathVars = Collections.singletonMap("dagId", dagId);
        return WebClientUtil.get(
            airflowWebClient,
            DAG_BASE_PATH,
            pathVars,
            queryParams,
            DagRunCollection.class,
            DAG_RUN_RESOURCE_NAME
        ).doOnSuccess(result -> log.info("Successfully retrieved {} DAG runs for DAG: {}", 
            result.getDagRuns() != null ? result.getDagRuns().size() : 0, dagId));
    }

    /**
     * Creates a new DAG Run
     * 
     * @param dagId The DAG identifier
     * @param dagRunCreate The creation parameters
     * @return A Mono containing the created DAG Run
     */
    public Mono<DagRun> createDagRun(String dagId, DagRunCreate dagRunCreate) {
        log.info("Creating DAG run for DAG: {}, configuration: {}", dagId, dagRunCreate);
        Map<String, Object> pathVars = Collections.singletonMap("dagId", dagId);
        return WebClientUtil.post(
            airflowWebClient,
            DAG_BASE_PATH,
            pathVars,
            dagRunCreate,
            DagRun.class,
            DAG_RUN_RESOURCE_NAME
        ).flatMap(dagRun -> {
            String actionDetails = "DAG Run triggered";
            if (dagRunCreate.getNote() != null && !dagRunCreate.getNote().isEmpty()) {
                actionDetails += " with note: " + dagRunCreate.getNote();
            }
            
            log.info("DAG run created successfully for DAG: {}, run ID: {}", dagId, dagRun.getDagRunId());
            
            return dagActionLogService
                .logDagAction(dagId, ActionType.TRIGGERED, actionDetails, true, dagRun.getDagRunId())
                .thenReturn(dagRun);
        });
    }
    
    /**
     * Retrieves a specific DAG Run
     * 
     * @param dagId The DAG identifier
     * @param dagRunId The DAG Run identifier
     * @return A Mono containing the requested DAG Run
     */
    public Mono<DagRun> getDagRun(String dagId, String dagRunId) {
        log.info("Retrieving DAG run: {} for DAG: {}", dagRunId, dagId);
        Map<String, Object> pathVars = new HashMap<>();
        pathVars.put("dagId", dagId);
        pathVars.put("dagRunId", dagRunId);
        
        return WebClientUtil.get(
            airflowWebClient,
            DAG_BASE_PATH + "/{dagRunId}",
            pathVars,
            null,
            DagRun.class,
            DAG_RUN_RESOURCE_NAME
        ).doOnSuccess(dagRun -> log.info("Successfully retrieved DAG run: {} for DAG: {}", dagRunId, dagId));
    }

    /**
     * Deletes a specific DAG Run
     * 
     * @param dagId The DAG identifier
     * @param dagRunId The DAG Run identifier
     * @return A Mono that completes when the DAG Run is deleted
     */
    public Mono<Void> deleteDagRun(String dagId, String dagRunId) {
        log.info("Deleting DAG run: {} for DAG: {}", dagRunId, dagId);
        Map<String, Object> pathVars = new HashMap<>();
        pathVars.put("dagId", dagId);
        pathVars.put("dagRunId", dagRunId);
        
        return WebClientUtil.delete(
            airflowWebClient,
            DAG_BASE_PATH + "/{dagRunId}",
            pathVars,
            DAG_RUN_RESOURCE_NAME
        ).then(
            dagActionLogService.logDagAction(
                dagId, 
                ActionType.DELETED, 
                "DAG Run deleted: " + dagRunId, 
                true, 
                dagRunId
            ).then(Mono.fromRunnable(() -> 
                log.info("Successfully deleted DAG run: {} for DAG: {}", dagRunId, dagId)
            ))
        );
    }

    /**
     * Updates the state of a specific DAG Run
     * 
     * @param dagId The DAG identifier
     * @param dagRunId The DAG Run identifier
     * @param stateUpdate The state update configuration
     * @return A Mono containing the updated DAG Run
     */
    public Mono<DagRun> updateDagRunState(String dagId, String dagRunId, DagRunStateUpdate stateUpdate) {
        log.info("Updating state of DAG run: {} for DAG: {} to: {}", dagRunId, dagId, stateUpdate.getState());
        Map<String, Object> pathVars = new HashMap<>();
        pathVars.put("dagId", dagId);
        pathVars.put("dagRunId", dagRunId);
        
        return WebClientUtil.patch(
            airflowWebClient,
            DAG_BASE_PATH + "/{dagRunId}",
            pathVars,
            stateUpdate,
            DagRun.class,
            DAG_RUN_RESOURCE_NAME
        ).flatMap(dagRun -> {
            String actionDetails = "DAG Run state updated to: " + stateUpdate.getState();
            ActionType actionType = ActionType.OTHER;
            
            log.info("Successfully updated state of DAG run: {} for DAG: {} to: {}", 
                dagRunId, dagId, stateUpdate.getState());
            
            return dagActionLogService.logDagAction(
                dagId, 
                actionType, 
                actionDetails, 
                true, 
                dagRunId
            ).thenReturn(dagRun);
        });
    }
    
    /**
     * Clears a specific DAG Run
     * 
     * @param dagId The DAG identifier
     * @param dagRunId The DAG Run identifier
     * @param clearRequest The clear operation parameters
     * @return A Mono containing the cleared DAG Run
     */
    public Mono<DagRun> clearDagRun(String dagId, String dagRunId, DagRunClear clearRequest) {
        log.info("Clearing DAG run: {} for DAG: {}", dagRunId, dagId);
        Map<String, Object> pathVars = new HashMap<>();
        pathVars.put("dagId", dagId);
        pathVars.put("dagRunId", dagRunId);
        
        return WebClientUtil.post(
            airflowWebClient,
            DAG_BASE_PATH + "/{dagRunId}/clear",
            pathVars,
            clearRequest,
            DagRun.class,
            DAG_RUN_RESOURCE_NAME
        ).flatMap(dagRun -> {
            log.info("Successfully cleared DAG run: {} for DAG: {}", dagRunId, dagId);
            
            return dagActionLogService.logDagAction(
                dagId, 
                ActionType.CLEARED, 
                "DAG Run cleared", 
                true, 
                dagRunId
            ).thenReturn(dagRun);
        });
    }

    /**
     * Retrieves upstream dataset events for a specific DAG Run
     * 
     * @param dagId The DAG identifier
     * @param dagRunId The DAG Run identifier
     * @return A Mono containing the dataset events
     */
    public Mono<DatasetEventCollection> getUpstreamDatasetEvents(String dagId, String dagRunId) {
        log.info("Retrieving upstream dataset events for DAG run: {} for DAG: {}", dagRunId, dagId);
        Map<String, Object> pathVars = new HashMap<>();
        pathVars.put("dagId", dagId);
        pathVars.put("dagRunId", dagRunId);
        
        return WebClientUtil.get(
            airflowWebClient,
            DAG_BASE_PATH + "/{dagRunId}/upstreamDatasetEvents",
            pathVars,
            null,
            DatasetEventCollection.class,
            DAG_RUN_RESOURCE_NAME
        ).doOnSuccess(events -> log.info("Successfully retrieved upstream dataset events for DAG run: {} for DAG: {}, count: {}", 
            dagRunId, dagId, events.getDatasetEvents() != null ? events.getDatasetEvents().size() : 0));
    }

    /**
     * Sets a note for a specific DAG Run
     * 
     * @param dagId The DAG identifier
     * @param dagRunId The DAG Run identifier
     * @param noteUpdate The note update
     * @return A Mono containing the updated DAG Run
     */
    public Mono<DagRun> setDagRunNote(String dagId, String dagRunId, DagRunNoteUpdate noteUpdate) {
        log.info("Setting note for DAG run: {} for DAG: {}, note: {}", dagRunId, dagId, noteUpdate.getNote());
        Map<String, Object> pathVars = new HashMap<>();
        pathVars.put("dagId", dagId);
        pathVars.put("dagRunId", dagRunId);
        
        return WebClientUtil.patch(
            airflowWebClient,
            DAG_BASE_PATH + "/{dagRunId}/setNote",
            pathVars,
            noteUpdate,
            DagRun.class,
            DAG_RUN_RESOURCE_NAME
        ).flatMap(dagRun -> {
            log.info("Successfully set note for DAG run: {} for DAG: {}", dagRunId, dagId);
            
            return dagActionLogService.logDagAction(
                dagId, 
                ActionType.OTHER, 
                "DAG Run note updated: " + noteUpdate.getNote(), 
                true, 
                dagRunId
            ).thenReturn(dagRun);
        });
    }
} 