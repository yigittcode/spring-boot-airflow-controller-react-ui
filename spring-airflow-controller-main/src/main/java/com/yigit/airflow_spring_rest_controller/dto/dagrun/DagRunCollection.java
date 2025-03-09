package com.yigit.airflow_spring_rest_controller.dto.dagrun;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@Data
public class DagRunCollection {
    @JsonProperty("dag_runs")
    private List<DagRun> dagRuns;
    
    @JsonProperty("total_entries")
    private Integer totalEntries;
} 