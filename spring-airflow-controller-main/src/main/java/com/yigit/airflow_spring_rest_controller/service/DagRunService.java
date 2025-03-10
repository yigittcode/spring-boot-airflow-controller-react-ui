package com.yigit.airflow_spring_rest_controller.service;

import com.yigit.airflow_spring_rest_controller.dto.dagrun.DagRun;
import com.yigit.airflow_spring_rest_controller.dto.dagrun.DagRunCollection;
import com.yigit.airflow_spring_rest_controller.dto.dagrun.DagRunCreate;
import com.yigit.airflow_spring_rest_controller.dto.dagrun.DagRunStateUpdate;
import com.yigit.airflow_spring_rest_controller.dto.dagrun.DagRunClear;
import com.yigit.airflow_spring_rest_controller.dto.dagrun.DagRunNoteUpdate;
import com.yigit.airflow_spring_rest_controller.dto.dataset.DatasetEventCollection;
import com.yigit.airflow_spring_rest_controller.entity.DagActionLog.ActionType;
import com.yigit.airflow_spring_rest_controller.exception.AirflowResourceNotFoundException;
import com.yigit.airflow_spring_rest_controller.exception.AirflowBadRequestException;
import com.yigit.airflow_spring_rest_controller.exception.AirflowConflictException;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class DagRunService {

    private final WebClient airflowWebClient;
    private final DagActionLogService dagActionLogService;

    public Mono<DagRunCollection> getDagRuns(String dagId, Map<String, String> queryParams) {
        return airflowWebClient.get()
            .uri(uriBuilder -> {
                var builder = uriBuilder.path("/dags/{dagId}/dagRuns");
                
                if (queryParams != null) {
                    queryParams.forEach((key, value) -> {
                        if (value != null) {
                            builder.queryParam(key, value);
                        }
                    });
                }
                
                return builder.build(dagId);
            })
            .retrieve()
            .onStatus(
                status -> status.value() == HttpStatus.NOT_FOUND.value(),
                response -> Mono.error(new AirflowResourceNotFoundException("DAG not found: " + dagId))
            )
            .bodyToMono(DagRunCollection.class);
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
                status -> status.value() == HttpStatus.BAD_REQUEST.value(),
                response -> Mono.error(new AirflowBadRequestException("Invalid request parameters"))
            )
            .onStatus(
                status -> status.value() == HttpStatus.CONFLICT.value(),
                response -> Mono.error(new AirflowConflictException("DAG Run already exists"))
            )
            .bodyToMono(DagRun.class)
            .flatMap(dagRun -> {
                String actionDetails = "DAG Run triggered";
                if (dagRunCreate.getNote() != null && !dagRunCreate.getNote().isEmpty()) {
                    actionDetails += " with note: " + dagRunCreate.getNote();
                }
                
                return dagActionLogService
                    .logDagAction(dagId, ActionType.TRIGGERED, actionDetails, true, dagRun.getDagRunId())
                    .thenReturn(dagRun);
            });
    }

    public Mono<DagRun> getDagRun(String dagId, String dagRunId) {
        return airflowWebClient.get()
            .uri("/dags/{dagId}/dagRuns/{dagRunId}", dagId, dagRunId)
            .retrieve()
            .onStatus(
                status -> status.value() == HttpStatus.NOT_FOUND.value(),
                response -> Mono.error(new AirflowResourceNotFoundException("DAG Run not found: " + dagId + "/" + dagRunId))
            )
            .bodyToMono(DagRun.class);
    }

    public Mono<Void> deleteDagRun(String dagId, String dagRunId) {
        return airflowWebClient.delete()
            .uri("/dags/{dagId}/dagRuns/{dagRunId}", dagId, dagRunId)
            .retrieve()
            .onStatus(
                status -> status.value() == HttpStatus.NOT_FOUND.value(),
                response -> Mono.error(new AirflowResourceNotFoundException("DAG Run not found: " + dagId + "/" + dagRunId))
            )
            .onStatus(
                status -> status.value() == HttpStatus.CONFLICT.value(),
                response -> Mono.error(new AirflowConflictException("DAG Run cannot be deleted in current state"))
            )
            .bodyToMono(Void.class)
            .flatMap(voidReturn -> 
                dagActionLogService.logDagAction(
                    dagId, 
                    ActionType.DELETED, 
                    "DAG Run deleted: " + dagRunId, 
                    true, 
                    dagRunId
                ).then()
            );
    }

    public Mono<DagRun> updateDagRunState(String dagId, String dagRunId, DagRunStateUpdate stateUpdate) {
        return airflowWebClient.patch()
            .uri("/dags/{dagId}/dagRuns/{dagRunId}", dagId, dagRunId)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(stateUpdate)
            .retrieve()
            .onStatus(
                status -> status.value() == HttpStatus.NOT_FOUND.value(),
                response -> Mono.error(new AirflowResourceNotFoundException("DAG Run not found: " + dagId + "/" + dagRunId))
            )
            .onStatus(
                status -> status.value() == HttpStatus.BAD_REQUEST.value(),
                response -> Mono.error(new AirflowBadRequestException("Invalid state transition"))
            )
            .bodyToMono(DagRun.class)
            .flatMap(dagRun -> {
                String actionDetails = "DAG Run state changed to: " + stateUpdate.getState();
                return dagActionLogService
                    .logDagAction(dagId, ActionType.OTHER, actionDetails, true, dagRunId)
                    .thenReturn(dagRun);
            });
    }

    public Mono<DagRun> clearDagRun(String dagId, String dagRunId, DagRunClear clearRequest) {
        return airflowWebClient.post()
            .uri("/dags/{dagId}/dagRuns/{dagRunId}/clear", dagId, dagRunId)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(clearRequest)
            .retrieve()
            .onStatus(
                status -> status.value() == HttpStatus.NOT_FOUND.value(),
                response -> Mono.error(new AirflowResourceNotFoundException("DAG Run not found: " + dagId + "/" + dagRunId))
            )
            .onStatus(
                status -> status.value() == HttpStatus.CONFLICT.value(),
                response -> Mono.error(new AirflowConflictException("Cannot clear DAG Run in current state"))
            )
            .bodyToMono(DagRun.class)
            .flatMap(dagRun -> {
                return dagActionLogService
                    .logDagAction(dagId, ActionType.CLEARED, "DAG Run cleared", true, dagRunId)
                    .thenReturn(dagRun);
            });
    }

    public Mono<DatasetEventCollection> getUpstreamDatasetEvents(String dagId, String dagRunId) {
        return airflowWebClient.get()
            .uri("/dags/{dagId}/dagRuns/{dagRunId}/upstreamDatasetEvents", dagId, dagRunId)
            .retrieve()
            .onStatus(
                status -> status.value() == HttpStatus.NOT_FOUND.value(),
                response -> Mono.error(new AirflowResourceNotFoundException("DAG Run not found: " + dagId + "/" + dagRunId))
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
                response -> Mono.error(new AirflowResourceNotFoundException("DAG Run not found: " + dagId + "/" + dagRunId))
            )
            .bodyToMono(DagRun.class)
            .flatMap(dagRun -> {
                String actionDetails = "Note updated: " + noteUpdate.getNote();
                return dagActionLogService
                    .logDagAction(dagId, ActionType.OTHER, actionDetails, true, dagRunId)
                    .thenReturn(dagRun);
            });
    }
} 