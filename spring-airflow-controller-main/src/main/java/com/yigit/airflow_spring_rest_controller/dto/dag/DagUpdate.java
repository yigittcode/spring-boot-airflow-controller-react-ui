package com.yigit.airflow_spring_rest_controller.dto.dag;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class DagUpdate {
    @JsonProperty("is_paused")
    private Boolean isPaused;
} 