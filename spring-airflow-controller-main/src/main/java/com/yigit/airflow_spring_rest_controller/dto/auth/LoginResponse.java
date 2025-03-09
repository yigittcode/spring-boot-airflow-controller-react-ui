package com.yigit.airflow_spring_rest_controller.dto.auth;

import com.yigit.airflow_spring_rest_controller.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {
    
    private String token;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private Role role;
} 