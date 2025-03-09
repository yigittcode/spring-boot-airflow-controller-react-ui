package com.yigit.airflow_spring_rest_controller.dto.dag;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.yigit.airflow_spring_rest_controller.dto.schedule.TimeDelta;
import java.time.ZonedDateTime;
import java.util.List;

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
} 