package com.yigit.airflow_spring_rest_controller.dto.task;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@Data
public class TaskCollection {
    private List<Task> tasks;
    
    @JsonProperty("total_entries")
    private Integer totalEntries;
} 