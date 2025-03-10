package com.yigit.airflow_spring_rest_controller.dto.log;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DagActionLogResponse {
    private List<DagActionLogDTO> logs;
    private Long totalCount;
    private Integer page;
    private Integer size;
} 