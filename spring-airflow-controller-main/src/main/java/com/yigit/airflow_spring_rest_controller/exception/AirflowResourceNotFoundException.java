package com.yigit.airflow_spring_rest_controller.exception;

public class AirflowResourceNotFoundException extends RuntimeException {
    public AirflowResourceNotFoundException(String message) {
        super(message);
    }
} 