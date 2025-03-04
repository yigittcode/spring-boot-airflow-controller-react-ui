package com.yigit.airflow_spring_rest_controller.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

@Service
public class LogService {

    private final WebClient airflowWebClient;

    @Autowired
    public LogService(WebClient airflowWebClient) {
        this.airflowWebClient = airflowWebClient;
    }

    public Mono<String> getTaskLogs(String dagId, String dagRunId, String taskId, Integer tryNumber) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder
            .fromPath("/dags/{dagId}/dagRuns/{dagRunId}/taskInstances/{taskId}/logs/{tryNumber}");

        return airflowWebClient.get()
            .uri(uriBuilder.buildAndExpand(dagId, dagRunId, taskId, tryNumber).toUriString())
            .retrieve()
            .bodyToMono(String.class);
    }
} 