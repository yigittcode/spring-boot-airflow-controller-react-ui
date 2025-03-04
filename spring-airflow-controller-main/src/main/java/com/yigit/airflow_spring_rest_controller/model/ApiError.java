package com.yigit.airflow_spring_rest_controller.model;

import lombok.Data;

@Data
public class ApiError {
    private String type;
    private String title;
    private Integer status;
    private String detail;
    private String instance;
} 