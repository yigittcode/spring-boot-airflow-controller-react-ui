package com.yigit.airflow_spring_rest_controller.model;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class DagRunClear {
    @JsonProperty("dry_run")
    private Boolean dryRun;
} 