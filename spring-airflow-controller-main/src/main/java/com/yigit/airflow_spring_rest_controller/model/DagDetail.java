package com.yigit.airflow_spring_rest_controller.model;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

@Data
public class DagDetail {
    @JsonProperty("dag_id")
    private String dagId;
    
    @JsonProperty("dag_display_name")
    private String dagDisplayName;
    
    @JsonProperty("root_dag_id")
    private String rootDagId;
    
    @JsonProperty("is_paused")
    private Boolean isPaused;
    
    @JsonProperty("is_active")
    private Boolean isActive;
    
    @JsonProperty("is_subdag")
    private Boolean isSubdag;
    
    @JsonProperty("last_parsed_time")
    private ZonedDateTime lastParsedTime;
    
    @JsonProperty("last_pickled")
    private ZonedDateTime lastPickled;
    
    @JsonProperty("last_expired")
    private ZonedDateTime lastExpired;
    
    @JsonProperty("scheduler_lock")
    private Boolean schedulerLock;
    
    @JsonProperty("pickle_id")
    private String pickleId;
    
    @JsonProperty("default_view")
    private String defaultView;
    
    private String fileloc;
    
    @JsonProperty("file_token")
    private String fileToken;
    
    private List<String> owners;
    
    private String description;
    
    @JsonProperty("schedule_interval")
    private TimeDelta scheduleInterval;
    
    @JsonProperty("timetable_description")
    private String timetableDescription;
    
    private List<Tag> tags;
    
    @JsonProperty("max_active_tasks")
    private Integer maxActiveTasks;
    
    @JsonProperty("max_active_runs")
    private Integer maxActiveRuns;
    
    @JsonProperty("has_task_concurrency_limits")
    private Boolean hasTaskConcurrencyLimits;
    
    @JsonProperty("has_import_errors")
    private Boolean hasImportErrors;
    
    @JsonProperty("next_dagrun")
    private ZonedDateTime nextDagrun;
    
    @JsonProperty("next_dagrun_data_interval_start")
    private ZonedDateTime nextDagrunDataIntervalStart;
    
    @JsonProperty("next_dagrun_data_interval_end")
    private ZonedDateTime nextDagrunDataIntervalEnd;
    
    @JsonProperty("next_dagrun_create_after")
    private ZonedDateTime nextDagrunCreateAfter;
    
    @JsonProperty("max_consecutive_failed_dag_runs")
    private Integer maxConsecutiveFailedDagRuns;
    
    private String timezone;
    
    private Boolean catchup;
    
    private String orientation;
    
    private Integer concurrency;
    
    @JsonProperty("start_date")
    private ZonedDateTime startDate;
    
    @JsonProperty("dag_run_timeout")
    private TimeDelta dagRunTimeout;
    
    @JsonProperty("dataset_expression")
    private Map<String, Object> datasetExpression;
    
    @JsonProperty("doc_md")
    private String docMd;
    
    private Map<String, Object> params;
    
    @JsonProperty("end_date")
    private ZonedDateTime endDate;
    
    @JsonProperty("is_paused_upon_creation")
    private Boolean isPausedUponCreation;
    
    @JsonProperty("last_parsed")
    private ZonedDateTime lastParsed;
    
    @JsonProperty("template_search_path")
    private List<String> templateSearchPath;
    
    @JsonProperty("render_template_as_native_obj")
    private Boolean renderTemplateAsNativeObj;
} 