package com.yigit.airflow_spring_rest_controller.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class AirflowBadRequestException extends RuntimeException {
    
    public AirflowBadRequestException(String message) {
        super(message);
    }
    
    public AirflowBadRequestException(String message, Throwable cause) {
        super(message, cause);
    }
} 