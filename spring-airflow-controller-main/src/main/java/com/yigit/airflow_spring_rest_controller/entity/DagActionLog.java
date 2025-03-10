package com.yigit.airflow_spring_rest_controller.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("dag_action_logs")
public class DagActionLog {
    
    @Id
    @Column("id")
    private Long id;
    
    @Column("username")
    private String username;
    
    @Column("dag_id")
    private String dagId;
    
    @Column("action_type")
    private String actionType;
    
    @Column("action_details")
    private String actionDetails;
    
    @Column("timestamp")
    private LocalDateTime timestamp;
    
    @Column("success")
    private Boolean success;
    
    @Column("run_id")
    private String runId;
    
    // Enum for action types
    public enum ActionType {
        TRIGGERED("TRIGGERED"),
        PAUSED("PAUSED"),
        UNPAUSED("UNPAUSED"),
        DELETED("DELETED"),
        CLEARED("CLEARED"),
        TASK_STATE_CHANGED("TASK_STATE_CHANGED"),
        OTHER("OTHER");
        
        private final String value;
        
        ActionType(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
        
        @Override
        public String toString() {
            return value;
        }
    }
} 