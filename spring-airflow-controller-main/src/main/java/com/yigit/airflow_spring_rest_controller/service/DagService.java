package com.yigit.airflow_spring_rest_controller.service;

import com.yigit.airflow_spring_rest_controller.dto.dag.Dag;
import com.yigit.airflow_spring_rest_controller.dto.dag.DagCollection;
import com.yigit.airflow_spring_rest_controller.dto.dag.DagDetail;
import com.yigit.airflow_spring_rest_controller.dto.dag.DagUpdate;
import com.yigit.airflow_spring_rest_controller.dto.task.TaskCollection;
import com.yigit.airflow_spring_rest_controller.dto.task.TaskInstanceCollection;
import com.yigit.airflow_spring_rest_controller.dto.task.TaskInstanceStateUpdate;
import com.yigit.airflow_spring_rest_controller.entity.DagActionLog.ActionType;
import com.yigit.airflow_spring_rest_controller.exception.AirflowResourceNotFoundException;
import com.yigit.airflow_spring_rest_controller.exception.AirflowConflictException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class DagService {

    private final WebClient airflowWebClient;
    private final DagActionLogService dagActionLogService;

    public Mono<DagCollection> getDags() {
        return airflowWebClient.get()
            .uri("/dags")
            .retrieve()
            .bodyToMono(DagCollection.class);
    }

    public Mono<Dag> getDag(String dagId) {
        return airflowWebClient.get()
            .uri("/dags/{dagId}", dagId)
            .retrieve()
            .onStatus(
                status -> status.value() == HttpStatus.NOT_FOUND.value(),
                response -> Mono.error(new AirflowResourceNotFoundException("DAG not found: " + dagId))
            )
            .bodyToMono(Dag.class);
    }

    public Mono<Dag> updateDag(String dagId, DagUpdate dagUpdate) {
        return airflowWebClient.patch()
            .uri("/dags/{dagId}", dagId)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(dagUpdate)
            .retrieve()
            .onStatus(
                status -> status.value() == HttpStatus.NOT_FOUND.value(),
                response -> Mono.error(new AirflowResourceNotFoundException("DAG not found: " + dagId))
            )
            .onStatus(
                status -> status.value() == HttpStatus.CONFLICT.value(),
                response -> Mono.error(new AirflowConflictException("DAG update conflict"))
            )
            .bodyToMono(Dag.class)
            .flatMap(dag -> {
                // Log the DAG action based on what was updated
                String actionDetails = "DAG updated";
                ActionType actionType = ActionType.OTHER;
                
                if (dagUpdate.getIsPaused() != null) {
                    actionType = dagUpdate.getIsPaused() ? ActionType.PAUSED : ActionType.UNPAUSED;
                    actionDetails = dagUpdate.getIsPaused() ? "DAG paused" : "DAG unpaused";
                }
                
                return dagActionLogService.logDagAction(dagId, actionType, actionDetails, true, null)
                    .thenReturn(dag);
            });
    }

    public Mono<Void> deleteDag(String dagId) {
        return airflowWebClient.delete()
            .uri("/dags/{dagId}", dagId)
            .retrieve()
            .onStatus(
                status -> status.value() == HttpStatus.NOT_FOUND.value(),
                response -> Mono.error(new AirflowResourceNotFoundException("DAG not found: " + dagId))
            )
            .onStatus(
                status -> status.value() == HttpStatus.CONFLICT.value(),
                response -> Mono.error(new AirflowConflictException("DAG cannot be deleted"))
            )
            .bodyToMono(Void.class)
            .flatMap(voidReturn -> 
                dagActionLogService.logDagAction(dagId, ActionType.DELETED, "DAG deleted", true, null)
                    .then()
            );
    }

    public Mono<TaskCollection> getDagTasks(String dagId) {
        return airflowWebClient.get()
            .uri("/dags/{dagId}/tasks", dagId)
            .retrieve()
            .onStatus(
                status -> status.value() == HttpStatus.NOT_FOUND.value(),
                response -> Mono.error(new AirflowResourceNotFoundException("DAG not found: " + dagId))
            )
            .bodyToMono(TaskCollection.class);
    }

    public Mono<DagDetail> getDagDetails(String dagId) {
        return airflowWebClient.get()
            .uri("/dags/{dagId}/details", dagId)
            .retrieve()
            .onStatus(
                status -> status.value() == HttpStatus.NOT_FOUND.value(),
                response -> Mono.error(new AirflowResourceNotFoundException("DAG not found: " + dagId))
            )
            .bodyToMono(DagDetail.class);
    }
} 