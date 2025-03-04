package com.yigit.airflow_spring_rest_controller.service;

import com.yigit.airflow_spring_rest_controller.model.*;
import com.yigit.airflow_spring_rest_controller.exception.AirflowResourceNotFoundException;
import com.yigit.airflow_spring_rest_controller.exception.AirflowConflictException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import java.util.Map;

@Service
public class DagRunService {

    private final WebClient airflowWebClient;

    @Autowired
    public DagRunService(WebClient airflowWebClient) {
        this.airflowWebClient = airflowWebClient;
    }

    public Mono<DagRunCollection> getDagRuns(String dagId, Map<String, String> queryParams) {
        return airflowWebClient.get()
            .uri(uriBuilder -> {
                uriBuilder = uriBuilder.path("/dags/{dagId}/dagRuns");
                
                // Add query parameters if they exist
                if (queryParams != null) {
                    for (Map.Entry<String, String> entry : queryParams.entrySet()) {
                        if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                            uriBuilder = uriBuilder.queryParam(entry.getKey(), entry.getValue());
                        }
                    }
                }
                
                return uriBuilder.build(dagId);
            })
            .retrieve()
            .onStatus(
                status -> status.value() == HttpStatus.NOT_FOUND.value(),
                response -> Mono.error(new AirflowResourceNotFoundException("DAG not found: " + dagId))
            )
            .bodyToMono(DagRunCollection.class);
    }
    
    // For backward compatibility
    public Mono<DagRunCollection> getDagRuns(String dagId) {
        return getDagRuns(dagId, null);
    }

    public Mono<DagRun> createDagRun(String dagId, DagRunCreate dagRunCreate) {
        return airflowWebClient.post()
            .uri("/dags/{dagId}/dagRuns", dagId)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(dagRunCreate)
            .retrieve()
            .onStatus(
                status -> status.value() == HttpStatus.NOT_FOUND.value(),
                response -> Mono.error(new AirflowResourceNotFoundException("DAG not found: " + dagId))
            )
            .onStatus(
                status -> status.value() == HttpStatus.CONFLICT.value(),
                response -> Mono.error(new AirflowConflictException("DAG Run already exists for the specified date"))
            )
            .bodyToMono(DagRun.class);
    }

    public Mono<DagRun> getDagRun(String dagId, String dagRunId) {
        return airflowWebClient.get()
            .uri("/dags/{dagId}/dagRuns/{dagRunId}", dagId, dagRunId)
            .retrieve()
            .onStatus(
                status -> status.value() == HttpStatus.NOT_FOUND.value(),
                response -> Mono.error(new AirflowResourceNotFoundException(
                    String.format("DAG Run not found: dagId=%s, dagRunId=%s", dagId, dagRunId)
                ))
            )
            .bodyToMono(DagRun.class);
    }

    public Mono<Void> deleteDagRun(String dagId, String dagRunId) {
        return airflowWebClient.delete()
            .uri("/dags/{dagId}/dagRuns/{dagRunId}", dagId, dagRunId)
            .retrieve()
            .onStatus(
                status -> status.value() == HttpStatus.NOT_FOUND.value(),
                response -> Mono.error(new AirflowResourceNotFoundException(
                    String.format("DAG Run not found: dagId=%s, dagRunId=%s", dagId, dagRunId)
                ))
            )
            .bodyToMono(Void.class);
    }

    public Mono<DagRun> updateDagRunState(String dagId, String dagRunId, DagRunStateUpdate stateUpdate) {
        return airflowWebClient.patch()
            .uri("/dags/{dagId}/dagRuns/{dagRunId}", dagId, dagRunId)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(stateUpdate)
            .retrieve()
            .onStatus(
                status -> status.value() == HttpStatus.NOT_FOUND.value(),
                response -> Mono.error(new AirflowResourceNotFoundException(
                    String.format("DAG Run not found: dagId=%s, dagRunId=%s", dagId, dagRunId)
                ))
            )
            .onStatus(
                status -> status.value() == HttpStatus.CONFLICT.value(),
                response -> Mono.error(new AirflowConflictException("Invalid state transition requested"))
            )
            .bodyToMono(DagRun.class);
    }

    public Mono<DagRun> clearDagRun(String dagId, String dagRunId, DagRunClear clearRequest) {
        return airflowWebClient.post()
            .uri("/dags/{dagId}/dagRuns/{dagRunId}/clear", dagId, dagRunId)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(clearRequest)
            .retrieve()
            .onStatus(
                status -> status.value() == HttpStatus.NOT_FOUND.value(),
                response -> Mono.error(new AirflowResourceNotFoundException(
                    String.format("DAG Run not found: dagId=%s, dagRunId=%s", dagId, dagRunId)
                ))
            )
            .onStatus(
                status -> status.value() == HttpStatus.CONFLICT.value(),
                response -> Mono.error(new AirflowConflictException("Cannot clear DAG Run in current state"))
            )
            .bodyToMono(DagRun.class);
    }

    public Mono<DatasetEventCollection> getUpstreamDatasetEvents(String dagId, String dagRunId) {
        return airflowWebClient.get()
            .uri("/dags/{dagId}/dagRuns/{dagRunId}/upstreamDatasetEvents", dagId, dagRunId)
            .retrieve()
            .onStatus(
                status -> status.value() == HttpStatus.NOT_FOUND.value(),
                response -> Mono.error(new AirflowResourceNotFoundException(
                    String.format("DAG Run not found: dagId=%s, dagRunId=%s", dagId, dagRunId)
                ))
            )
            .bodyToMono(DatasetEventCollection.class);
    }

    public Mono<DagRun> setDagRunNote(String dagId, String dagRunId, DagRunNoteUpdate noteUpdate) {
        return airflowWebClient.patch()
            .uri("/dags/{dagId}/dagRuns/{dagRunId}/setNote", dagId, dagRunId)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(noteUpdate)
            .retrieve()
            .onStatus(
                status -> status.value() == HttpStatus.NOT_FOUND.value(),
                response -> Mono.error(new AirflowResourceNotFoundException(
                    String.format("DAG Run not found: dagId=%s, dagRunId=%s", dagId, dagRunId)
                ))
            )
            .bodyToMono(DagRun.class);
    }
} 