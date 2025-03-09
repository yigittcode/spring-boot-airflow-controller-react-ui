package com.yigit.airflow_spring_rest_controller.service;

import com.yigit.airflow_spring_rest_controller.dto.dag.Dag;
import com.yigit.airflow_spring_rest_controller.dto.dag.DagCollection;
import com.yigit.airflow_spring_rest_controller.dto.dag.DagDetail;
import com.yigit.airflow_spring_rest_controller.dto.dag.DagUpdate;
import com.yigit.airflow_spring_rest_controller.dto.task.TaskCollection;
import com.yigit.airflow_spring_rest_controller.dto.task.TaskInstanceCollection;
import com.yigit.airflow_spring_rest_controller.dto.task.TaskInstanceStateUpdate;
import com.yigit.airflow_spring_rest_controller.exception.AirflowResourceNotFoundException;
import com.yigit.airflow_spring_rest_controller.exception.AirflowConflictException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class DagService {

    private final WebClient airflowWebClient;

    @Autowired
    public DagService(WebClient airflowWebClient) {
        this.airflowWebClient = airflowWebClient;
    }

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
                response -> Mono.error(new AirflowConflictException("Cannot update DAG in current state"))
            )
            .bodyToMono(Dag.class);
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
                response -> Mono.error(new AirflowConflictException("Cannot delete DAG with active runs"))
            )
            .bodyToMono(Void.class);
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