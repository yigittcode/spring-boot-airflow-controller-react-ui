package com.yigit.airflow_spring_rest_controller.dto.task;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@Data
public class TaskInstanceCollection {
    @JsonProperty("task_instances")
    private List<TaskInstance> taskInstances;
} 