package com.yigit.airflow_spring_rest_controller.dto.dataset;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@Data
public class DatasetEventCollection {
    @JsonProperty("dataset_events")
    private List<DatasetEvent> datasetEvents;
    
    @JsonProperty("total_entries")
    private Integer totalEntries;
} 