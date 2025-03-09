package com.yigit.airflow_spring_rest_controller.dto.dagrun;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.ZonedDateTime;
import java.util.Map;

@Data
public class DagRunCreate {
    @JsonProperty("dag_run_id")
    private String dagRunId;
    
    @JsonProperty("logical_date")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private ZonedDateTime logicalDate;
    
    @JsonProperty("execution_date")
    @Deprecated
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private ZonedDateTime executionDate;
    
    @JsonProperty("data_interval_start")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private ZonedDateTime dataIntervalStart;
    
    @JsonProperty("data_interval_end")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private ZonedDateTime dataIntervalEnd;
    
    private Map<String, Object> conf;
    
    private String note;

    public void setLogicalDate(ZonedDateTime logicalDate) {
        this.logicalDate = logicalDate;
        this.executionDate = logicalDate;
    }

    public void setExecutionDate(ZonedDateTime executionDate) {
        this.executionDate = executionDate;
        this.logicalDate = executionDate;
    }
} 