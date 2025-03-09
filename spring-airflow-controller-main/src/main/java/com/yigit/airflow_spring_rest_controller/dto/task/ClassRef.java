package com.yigit.airflow_spring_rest_controller.dto.task;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class ClassRef {
    @JsonProperty("__type")
    private String type;
    
    @JsonProperty("class_name")
    private String className;
    
    @JsonProperty("module_path")
    private String modulePath;
} 