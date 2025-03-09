package com.yigit.airflow_spring_rest_controller.dto.dag;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.yigit.airflow_spring_rest_controller.dto.schedule.ScheduleInterval;
import java.time.ZonedDateTime;
import java.util.List;

@Data
public class Dag {
    @JsonProperty("dag_display_name")
    private String dagDisplayName;
    
    @JsonProperty("dag_id")
    private String dagId;
    
    @JsonProperty("default_view")
    private String defaultView;
    
    private String description;
    
    @JsonProperty("file_token")
    private String fileToken;
    
    private String fileloc;
    
    @JsonProperty("has_import_errors")
    private Boolean hasImportErrors;
    
    @JsonProperty("has_task_concurrency_limits")
    private Boolean hasTaskConcurrencyLimits;
    
    @JsonProperty("is_active")
    private Boolean isActive;
    
    @JsonProperty("is_paused")
    private Boolean isPaused;
    
    @JsonProperty("is_subdag")
    private Boolean isSubdag;
    
    @JsonProperty("last_expired")
    private ZonedDateTime lastExpired;
    
    @JsonProperty("last_parsed_time")
    private ZonedDateTime lastParsedTime;
    
    @JsonProperty("last_pickled")
    private ZonedDateTime lastPickled;
    
    @JsonProperty("max_active_runs")
    private Integer maxActiveRuns;
    
    @JsonProperty("max_active_tasks")
    private Integer maxActiveTasks;
    
    @JsonProperty("max_consecutive_failed_dag_runs")
    private Integer maxConsecutiveFailedDagRuns;
    
    @JsonProperty("next_dagrun")
    private ZonedDateTime nextDagrun;
    
    @JsonProperty("next_dagrun_create_after")
    private ZonedDateTime nextDagrunCreateAfter;
    
    @JsonProperty("next_dagrun_data_interval_end")
    private ZonedDateTime nextDagrunDataIntervalEnd;
    
    @JsonProperty("next_dagrun_data_interval_start")
    private ZonedDateTime nextDagrunDataIntervalStart;
    
    private List<String> owners;
    
    @JsonProperty("pickle_id")
    private String pickleId;
    
    @JsonProperty("root_dag_id")
    private String rootDagId;
    
    @JsonProperty("schedule_interval")
    private ScheduleInterval scheduleInterval;
    
    @JsonProperty("scheduler_lock")
    private Boolean schedulerLock;
    
    private List<Tag> tags;
    
    @JsonProperty("timetable_description")
    private String timetableDescription;
} 