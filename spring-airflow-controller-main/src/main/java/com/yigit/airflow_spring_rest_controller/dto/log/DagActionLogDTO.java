package com.yigit.airflow_spring_rest_controller.dto.log;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DagActionLogDTO {
    private Long id;
    private String username;
    private String dagId;
    private String actionType;
    private String actionDetails;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "UTC")
    private LocalDateTime timestamp;
    
    private Boolean success;
    private String runId;
} 