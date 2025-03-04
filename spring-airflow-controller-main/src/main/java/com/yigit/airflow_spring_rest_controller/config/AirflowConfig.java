package com.yigit.airflow_spring_rest_controller.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.HttpHeaders;
import java.util.Base64;

@Configuration
public class AirflowConfig {

    private static final String API_VERSION = "/api/v1";

    @Value("${airflow.api.base-url}")
    private String baseUrl;

    @Value("${airflow.api.username}")
    private String username;

    @Value("${airflow.api.password}")
    private String password;

    @Bean
    public WebClient airflowWebClient() {
        String credentials = username + ":" + password;
        String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());

        return WebClient.builder()
            .baseUrl(baseUrl + API_VERSION)
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + encodedCredentials)
            .build();
    }
} 