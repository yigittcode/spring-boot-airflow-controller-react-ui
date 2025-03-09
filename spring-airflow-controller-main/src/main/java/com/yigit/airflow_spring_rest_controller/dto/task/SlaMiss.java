package com.yigit.airflow_spring_rest_controller.dto.task;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.ZonedDateTime;

@Data
public class SlaMiss {
    @JsonProperty("task_id")
    private String taskId;
    
    @JsonProperty("dag_id")
    private String dagId;
    
    @JsonProperty("execution_date")
    private ZonedDateTime executionDate;
    
    @JsonProperty("email_sent")
    private Boolean emailSent;
    
    private ZonedDateTime timestamp;
    
    private String description;
    
    @JsonProperty("notification_sent")
    private Boolean notificationSent;
} 