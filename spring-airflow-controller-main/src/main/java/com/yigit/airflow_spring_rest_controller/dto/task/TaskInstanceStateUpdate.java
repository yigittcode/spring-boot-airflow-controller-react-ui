package com.yigit.airflow_spring_rest_controller.dto.task;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.ZonedDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaskInstanceStateUpdate {
    @JsonProperty("new_state")
    private String newState;
    
    @JsonProperty("dry_run")
    private boolean dryRun = false;
    
    @JsonProperty("task_id")
    private String taskId;
    
    @JsonProperty("execution_date")
    private ZonedDateTime executionDate;
    
    @JsonProperty("dag_run_id")
    private String dagRunId;
    
    @JsonProperty("include_upstream")
    private Boolean includeUpstream;
    
    @JsonProperty("include_downstream")
    private Boolean includeDownstream;
    
    @JsonProperty("include_future")
    private Boolean includeFuture;
    
    @JsonProperty("include_past")
    private Boolean includePast;
} 