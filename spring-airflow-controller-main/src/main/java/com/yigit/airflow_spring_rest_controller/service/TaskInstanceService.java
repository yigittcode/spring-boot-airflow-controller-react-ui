package com.yigit.airflow_spring_rest_controller.service;

import com.yigit.airflow_spring_rest_controller.dto.task.TaskInstance;
import com.yigit.airflow_spring_rest_controller.dto.task.TaskInstanceCollection;
import com.yigit.airflow_spring_rest_controller.util.WebClientUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for interacting with Airflow Task Instances
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TaskInstanceService {
    
    private final WebClient airflowWebClient;
    
    private static final String TASK_INSTANCE_RESOURCE_NAME = "Task Instance";
    
    /**
     * Retrieves a specific task instance
     * 
     * @param dagId The DAG identifier
     * @param dagRunId The DAG run identifier
     * @param taskId The task identifier
     * @return A Mono containing the task instance
     */
    public Mono<TaskInstance> getTaskInstance(String dagId, String dagRunId, String taskId) {
        log.info("Retrieving task instance: {}, for DAG run: {}, DAG: {}", taskId, dagRunId, dagId);
        Map<String, Object> pathVars = new HashMap<>();
        pathVars.put("dagId", dagId);
        pathVars.put("dagRunId", dagRunId);
        pathVars.put("taskId", taskId);
        
        return WebClientUtil.get(
            airflowWebClient,
            "/dags/{dagId}/dagRuns/{dagRunId}/taskInstances/{taskId}",
            pathVars,
            null,
            TaskInstance.class,
            TASK_INSTANCE_RESOURCE_NAME
        ).doOnSuccess(task -> log.info("Successfully retrieved task instance: {}, state: {}", 
            taskId, task.getState()));
    }

    /**
     * Retrieves task instances for a specific DAG run
     * 
     * @param dagId The DAG identifier
     * @param dagRunId The DAG run identifier
     * @param queryParams Optional query parameters for filtering
     * @return A Mono containing the task instances
     */
    public Mono<TaskInstanceCollection> getTaskInstances(
            String dagId, 
            String dagRunId, 
            Map<String, List<String>> queryParams
    ) {
        log.info("Retrieving task instances for DAG run: {}, DAG: {}, filters: {}", dagRunId, dagId, queryParams);
        Map<String, Object> pathVars = new HashMap<>();
        pathVars.put("dagId", dagId);
        pathVars.put("dagRunId", dagRunId);
        
        // Convert multi-value map to single value map for WebClientUtil
        Map<String, String> flattenedParams = null;
        if (queryParams != null && !queryParams.isEmpty()) {
            flattenedParams = queryParams.entrySet().stream()
                .filter(entry -> entry.getValue() != null && !entry.getValue().isEmpty())
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    entry -> String.join(",", entry.getValue())
                ));
            log.debug("Flattened query parameters: {}", flattenedParams);
        }
        
        return WebClientUtil.get(
            airflowWebClient,
            "/dags/{dagId}/dagRuns/{dagRunId}/taskInstances",
            pathVars,
            flattenedParams,
            TaskInstanceCollection.class,
            TASK_INSTANCE_RESOURCE_NAME
        ).doOnSuccess(tasks -> log.info("Successfully retrieved {} task instances for DAG run: {}, DAG: {}", 
            tasks.getTaskInstances() != null ? tasks.getTaskInstances().size() : 0, dagRunId, dagId));
    }
} 