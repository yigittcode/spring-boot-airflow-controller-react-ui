package com.yigit.airflow_spring_rest_controller.dto.dag;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@Data
public class DagCollection {
    private List<Dag> dags;
    
    @JsonProperty("total_entries")
    private Integer totalEntries;
} 