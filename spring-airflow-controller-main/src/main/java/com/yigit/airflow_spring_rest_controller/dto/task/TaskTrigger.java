package com.yigit.airflow_spring_rest_controller.dto.task;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.ZonedDateTime;

@Data
public class TaskTrigger {
    private Integer id;
    
    private String classpath;
    
    private String kwargs;
    
    @JsonProperty("created_date")
    private ZonedDateTime createdDate;
    
    @JsonProperty("triggerer_id")
    private Integer triggererId;
} 