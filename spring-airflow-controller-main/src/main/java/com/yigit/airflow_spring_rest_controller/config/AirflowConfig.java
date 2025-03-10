package com.yigit.airflow_spring_rest_controller.config;

import com.yigit.airflow_spring_rest_controller.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Base64;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class AirflowConfig {

    private static final String API_VERSION = "/api/v1";
    
    private final JwtUtil jwtUtil;

    @Value("${airflow.api.base-url}")
    private String baseUrl;
    
    @Bean
    public WebClient airflowWebClient() {
        return WebClient.builder()
            .baseUrl(baseUrl + API_VERSION)
            .filter(authFilter())
            .build();
    }
    
    private ExchangeFilterFunction authFilter() {
        return ExchangeFilterFunction.ofRequestProcessor(request -> 
            ReactiveSecurityContextHolder.getContext()
                .map(context -> {
                    Authentication auth = context.getAuthentication();
                    if (auth != null && auth.getCredentials() != null) {
                        String token = auth.getCredentials().toString();
                        if (token != null && !token.isEmpty()) {
                            try {
                                String airflowUsername = jwtUtil.extractAirflowUsername(token);
                                String airflowPassword = jwtUtil.extractAirflowPassword(token);
                                
                                if (airflowUsername != null && airflowPassword != null) {
                                    String credentials = airflowUsername + ":" + airflowPassword;
                                    String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());
                                    return ClientRequest.from(request)
                                        .header(HttpHeaders.AUTHORIZATION, "Basic " + encodedCredentials)
                                        .build();
                                }
                            } catch (Exception e) {
                                log.warn("Error extracting Airflow credentials from JWT: {}", e.getMessage());
                            }
                        }
                    }
                    // Default fallback - can optionally use default credentials if needed
                    return request;
                })
                .defaultIfEmpty(request)
        );
    }
} 