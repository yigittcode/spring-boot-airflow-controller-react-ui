package com.yigit.airflow_spring_rest_controller.model;

import lombok.Data;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class TaskInstanceCollection {
    @JsonProperty("task_instances")
    private List<TaskInstance> taskInstances;
} 