package com.yigit.airflow_spring_rest_controller.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("users")
public class User {
    
    @Id
    private Long id;
    
    @Column("first_name")
    private String firstName;
    
    @Column("last_name")
    private String lastName;
    
    @Column("username")
    private String username;
    
    @Column("email")
    private String email;
    
    @Column("password")
    private String password;
    
    @Column("is_active")
    private Boolean isActive;
    
    @Column("role")
    private Role role;
    
    // Additional fields for Airflow integration
    @Column("airflow_username")
    private String airflowUsername;
    
    @Column("airflow_password")
    private String airflowPassword;
} 