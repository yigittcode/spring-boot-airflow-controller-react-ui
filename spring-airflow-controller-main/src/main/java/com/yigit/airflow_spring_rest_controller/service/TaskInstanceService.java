package com.yigit.airflow_spring_rest_controller.service;

import com.yigit.airflow_spring_rest_controller.dto.task.TaskInstance;
import com.yigit.airflow_spring_rest_controller.dto.task.TaskInstanceCollection;
import com.yigit.airflow_spring_rest_controller.exception.AirflowResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Mono;
import java.util.List;
import java.util.Map;

@Service
public class TaskInstanceService {
    
    private final WebClient airflowWebClient;

    @Autowired
    public TaskInstanceService(WebClient airflowWebClient) {
        this.airflowWebClient = airflowWebClient;
    }

    public Mono<TaskInstance> getTaskInstance(String dagId, String dagRunId, String taskId) {
        return airflowWebClient.get()
            .uri("/dags/{dagId}/dagRuns/{dagRunId}/taskInstances/{taskId}", 
                dagId, dagRunId, taskId)
            .retrieve()
            .onStatus(
                status -> status.value() == HttpStatus.NOT_FOUND.value(),
                response -> Mono.error(new AirflowResourceNotFoundException(
                    String.format("Task instance not found: dagId=%s, dagRunId=%s, taskId=%s", 
                        dagId, dagRunId, taskId)
                ))
            )
            .bodyToMono(TaskInstance.class);
    }

    public Mono<TaskInstanceCollection> getTaskInstances(
            String dagId, 
            String dagRunId, 
            Map<String, List<String>> queryParams
    ) {
        return airflowWebClient.get()
            .uri(uriBuilder -> {
                MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
                if (queryParams != null) {
                    queryParams.forEach(params::addAll);
                }
                
                uriBuilder.path("/dags/{dagId}/dagRuns/{dagRunId}/taskInstances")
                    .queryParams(params);
                return uriBuilder.build(dagId, dagRunId);
            })
            .retrieve()
            .onStatus(
                status -> status.value() == HttpStatus.NOT_FOUND.value(),
                response -> Mono.error(new AirflowResourceNotFoundException(
                    String.format("DAG Run not found: dagId=%s, dagRunId=%s", dagId, dagRunId)
                ))
            )
            .bodyToMono(TaskInstanceCollection.class);
    }
} 