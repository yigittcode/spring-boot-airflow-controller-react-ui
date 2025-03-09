package com.yigit.airflow_spring_rest_controller.dto.task;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.yigit.airflow_spring_rest_controller.dto.schedule.TimeDelta;
import java.util.List;
import java.util.Map;

@Data
public class Task {
    @JsonProperty("class_ref")
    private ClassRef classRef;
    
    @JsonProperty("task_id")
    private String taskId;
    
    @JsonProperty("task_display_name")
    private String taskDisplayName;
    
    @JsonProperty("trigger_rule")
    private String triggerRule;
    
    private String owner;
    
    @JsonProperty("start_date")
    private String startDate;
    
    @JsonProperty("end_date")
    private String endDate;
    
    @JsonProperty("depends_on_past")
    private Boolean dependsOnPast;
    
    @JsonProperty("wait_for_downstream")
    private Boolean waitForDownstream;
    
    @JsonProperty("retries")
    private Double retries;
    
    @JsonProperty("queue")
    private String queue;
    
    @JsonProperty("pool")
    private String pool;
    
    @JsonProperty("pool_slots")
    private Double poolSlots;
    
    @JsonProperty("execution_timeout")
    private TimeDelta executionTimeout;
    
    @JsonProperty("retry_delay")
    private TimeDelta retryDelay;
    
    @JsonProperty("retry_exponential_backoff")
    private Boolean retryExponentialBackoff;
    
    @JsonProperty("priority_weight")
    private Double priorityWeight;
    
    @JsonProperty("weight_rule")
    private String weightRule;
    
    @JsonProperty("ui_color")
    private String uiColor;
    
    @JsonProperty("ui_fgcolor")
    private String uiFgcolor;
    
    @JsonProperty("template_fields")
    private List<String> templateFields;
    
    @JsonProperty("downstream_task_ids")
    private List<String> downstreamTaskIds;
    
    @JsonProperty("extra_links")
    private List<String> extraLinks;
    
    private Map<String, Object> params;
    
    @JsonProperty("operator_name")
    private String operatorName;
    
    @JsonProperty("is_mapped")
    private Boolean isMapped;
    
    @JsonProperty("doc_md")
    private String docMd;
} 