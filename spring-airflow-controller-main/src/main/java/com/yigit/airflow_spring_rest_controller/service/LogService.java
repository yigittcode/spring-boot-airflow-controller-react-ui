package com.yigit.airflow_spring_rest_controller.service;

import com.yigit.airflow_spring_rest_controller.util.WebClientUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * Service for retrieving logs from Airflow
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LogService {

    private final WebClient airflowWebClient;
    
    private static final String LOG_RESOURCE_NAME = "Log";
    
    /**
     * Retrieves task logs for a specific task instance
     * 
     * @param dagId The DAG identifier
     * @param dagRunId The DAG run identifier
     * @param taskId The task identifier
     * @param tryNumber The try number
     * @return A Mono containing the task logs as string
     */
    public Mono<String> getTaskLogs(String dagId, String dagRunId, String taskId, Integer tryNumber) {
        log.info("Retrieving logs for task: {}, run: {}, dag: {}, try: {}", taskId, dagRunId, dagId, tryNumber);
        Map<String, Object> pathVars = new HashMap<>();
        pathVars.put("dagId", dagId);
        pathVars.put("dagRunId", dagRunId);
        pathVars.put("taskId", taskId);
        pathVars.put("tryNumber", tryNumber);
        
        return WebClientUtil.get(
            airflowWebClient,
            "/dags/{dagId}/dagRuns/{dagRunId}/taskInstances/{taskId}/logs/{tryNumber}",
            pathVars,
            null,
            String.class,
            LOG_RESOURCE_NAME
        ).doOnSuccess(logs -> {
            int logSize = logs != null ? logs.length() : 0;
            log.info("Successfully retrieved logs for task: {}, run: {}, dag: {}, size: {} characters", 
                taskId, dagRunId, dagId, logSize);
        });
    }
} 