package com.yigit.airflow_spring_rest_controller.dto.schedule;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class TimeDelta {
    @JsonProperty("__type")
    private String type;
    
    private Integer days;
    private Integer microseconds;
    private Integer seconds;
} 