package com.yigit.airflow_spring_rest_controller.exception;

public class AirflowConflictException extends RuntimeException {
    public AirflowConflictException(String message) {
        super(message);
    }
} 