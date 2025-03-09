package com.yigit.airflow_spring_rest_controller.dto.error;

import lombok.Data;

@Data
public class ApiError {
    private String type;
    private String title;
    private Integer status;
    private String detail;
    private String instance;
} 