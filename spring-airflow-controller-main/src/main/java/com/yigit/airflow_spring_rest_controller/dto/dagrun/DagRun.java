package com.yigit.airflow_spring_rest_controller.dto.dagrun;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.ZonedDateTime;
import java.util.Map;

@Data
public class DagRun {
    @JsonProperty("dag_id")
    private String dagId;
    
    @JsonProperty("dag_run_id")
    private String dagRunId;
    
    @JsonProperty("logical_date")
    private ZonedDateTime logicalDate;
    
    @JsonProperty("execution_date")
    private ZonedDateTime executionDate;
    
    @JsonProperty("start_date")
    private ZonedDateTime startDate;
    
    @JsonProperty("end_date")
    private ZonedDateTime endDate;
    
    @JsonProperty("data_interval_start")
    private ZonedDateTime dataIntervalStart;
    
    @JsonProperty("data_interval_end")
    private ZonedDateTime dataIntervalEnd;
    
    @JsonProperty("last_scheduling_decision")
    private ZonedDateTime lastSchedulingDecision;
    
    @JsonProperty("run_type")
    private String runType;
    
    private String state;
    
    @JsonProperty("external_trigger")
    private Boolean externalTrigger;
    
    private Map<String, Object> conf;
    
    private String note;
} 