package com.yigit.airflow_spring_rest_controller.repository;

import com.yigit.airflow_spring_rest_controller.entity.DagActionLog;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Repository
public interface DagActionLogRepository extends ReactiveCrudRepository<DagActionLog, Long> {
    
    Flux<DagActionLog> findByDagId(String dagId);
    
    @Query("SELECT * FROM dag_action_logs ORDER BY timestamp DESC LIMIT :limit OFFSET :offset")
    Flux<DagActionLog> findAllWithPagination(@Param("limit") int limit, @Param("offset") int offset);
    
    @Query("SELECT * FROM dag_action_logs WHERE username = :username ORDER BY timestamp DESC")
    Flux<DagActionLog> findByUsername(@Param("username") String username);
    
    @Query("SELECT * FROM dag_action_logs WHERE dag_id = :dagId ORDER BY timestamp DESC")
    Flux<DagActionLog> findByDagIdOrderByTimestampDesc(@Param("dagId") String dagId);
    
    @Query("SELECT * FROM dag_action_logs WHERE action_type = :actionType ORDER BY timestamp DESC")
    Flux<DagActionLog> findByActionType(@Param("actionType") String actionType);
    
    @Query("SELECT COUNT(*) FROM dag_action_logs")
    Flux<Long> countAll();
    
    @Modifying
    @Query("INSERT INTO dag_action_logs (username, dag_id, action_type, action_details, timestamp, success, run_id) " +
           "VALUES (:username, :dagId, :actionType, :actionDetails, :timestamp, :success, :runId)")
    Mono<Void> insertDagActionLog(
            @Param("username") String username,
            @Param("dagId") String dagId,
            @Param("actionType") String actionType,
            @Param("actionDetails") String actionDetails,
            @Param("timestamp") LocalDateTime timestamp,
            @Param("success") Boolean success,
            @Param("runId") String runId
    );
} 