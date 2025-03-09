package com.yigit.airflow_spring_rest_controller.dto.task;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.ZonedDateTime;

@Data
public class TriggererJob {
    private Integer id;
    
    @JsonProperty("dag_id")
    private String dagId;
    
    private String state;
    
    @JsonProperty("job_type")
    private String jobType;
    
    @JsonProperty("start_date")
    private ZonedDateTime startDate;
    
    @JsonProperty("end_date")
    private ZonedDateTime endDate;
    
    @JsonProperty("latest_heartbeat")
    private ZonedDateTime latestHeartbeat;
    
    @JsonProperty("executor_class")
    private String executorClass;
    
    private String hostname;
    
    private String unixname;
} 