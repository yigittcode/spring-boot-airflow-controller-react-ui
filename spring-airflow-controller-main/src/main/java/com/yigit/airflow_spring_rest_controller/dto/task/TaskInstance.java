package com.yigit.airflow_spring_rest_controller.dto.task;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.ZonedDateTime;
import java.util.Map;

@Data
public class TaskInstance {
    @JsonProperty("task_id")
    private String taskId;
    
    @JsonProperty("task_display_name")
    private String taskDisplayName;
    
    @JsonProperty("dag_id")
    private String dagId;
    
    @JsonProperty("dag_run_id")
    private String dagRunId;
    
    @JsonProperty("execution_date")
    private ZonedDateTime executionDate;
    
    @JsonProperty("start_date")
    private ZonedDateTime startDate;
    
    @JsonProperty("end_date")
    private ZonedDateTime endDate;
    
    private Double duration;
    
    private String state;
    
    @JsonProperty("try_number")
    private Integer tryNumber;
    
    @JsonProperty("map_index")
    private Integer mapIndex;
    
    @JsonProperty("max_tries")
    private Integer maxTries;
    
    private String hostname;
    
    private String unixname;
    
    private String pool;
    
    @JsonProperty("pool_slots")
    private Integer poolSlots;
    
    private String queue;
    
    @JsonProperty("priority_weight")
    private Integer priorityWeight;
    
    private String operator;
    
    @JsonProperty("queued_when")
    private ZonedDateTime queuedWhen;
    
    private Integer pid;
    
    private String executor;
    
    @JsonProperty("executor_config")
    private String executorConfig;
    
    @JsonProperty("sla_miss")
    private SlaMiss slaMiss;
    
    @JsonProperty("rendered_map_index")
    private String renderedMapIndex;
    
    @JsonProperty("rendered_fields")
    private Map<String, Object> renderedFields;
    
    private TaskTrigger trigger;
    
    @JsonProperty("triggerer_job")
    private TriggererJob triggererJob;
    
    private String note;
} 