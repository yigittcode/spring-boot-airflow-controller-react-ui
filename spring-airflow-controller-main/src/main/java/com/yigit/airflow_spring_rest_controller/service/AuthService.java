package com.yigit.airflow_spring_rest_controller.service;

import com.yigit.airflow_spring_rest_controller.dto.auth.LoginRequest;
import com.yigit.airflow_spring_rest_controller.dto.auth.LoginResponse;
import com.yigit.airflow_spring_rest_controller.repository.UserRepository;
import com.yigit.airflow_spring_rest_controller.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final ReactiveAuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public Mono<LoginResponse> login(LoginRequest request) {
        return authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()))
                .then(userRepository.findByUsername(request.getUsername()))
                .map(user -> {
                    String token = jwtUtil.generateToken(user);
                    return LoginResponse.builder()
                            .token(token)
                            .username(user.getUsername())
                            .firstName(user.getFirstName())
                            .lastName(user.getLastName())
                            .email(user.getEmail())
                            .role(user.getRole())
                            .build();
                });
    }
} 