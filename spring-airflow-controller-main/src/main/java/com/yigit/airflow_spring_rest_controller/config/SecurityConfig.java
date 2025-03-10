package com.yigit.airflow_spring_rest_controller.config;

import com.yigit.airflow_spring_rest_controller.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collections;

/**
 * Spring Security configuration for the application.
 * Implements Apache Airflow style RBAC (Role Based Access Control)
 */
@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ReactiveUserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public ReactiveAuthenticationManager authenticationManager() {
        UserDetailsRepositoryReactiveAuthenticationManager authManager = 
            new UserDetailsRepositoryReactiveAuthenticationManager(userDetailsService);
        authManager.setPasswordEncoder(passwordEncoder());
        return authManager;
    }

    /**
     * Configures security for the application endpoints based on Airflow roles
     */
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .cors(corsSpec -> corsSpec.configurationSource(corsConfigurationSource()))
            .authorizeExchange(exchanges -> exchanges
                // Public endpoints - no authentication required
                .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .pathMatchers("/api/auth/**").permitAll()
                
                // Swagger UI and API Docs
                .pathMatchers("/v3/api-docs/**", 
                             "/swagger-ui/**", 
                             "/swagger-ui.html",
                             "/webjars/**",
                             "/swagger-resources/**").permitAll()
                
                // DAG READ access - VIEWER and above (all authenticated users)
                // These endpoints only return data, cannot modify anything
                .pathMatchers(HttpMethod.GET, "/api/v1/dags").authenticated()
                .pathMatchers(HttpMethod.GET, "/api/v1/dags/*/details").authenticated()
                .pathMatchers(HttpMethod.GET, "/api/v1/dags/*/tasks").authenticated()
                .pathMatchers(HttpMethod.GET, "/api/v1/dags/*").authenticated()
                
                // DAG RUN access - USER and above (can execute but not modify DAGs)
                // These endpoints allow viewing and triggering DAG runs
                .pathMatchers(HttpMethod.GET, "/api/v1/dags/*/dagRuns").hasAnyRole("ADMIN", "OP", "USER")
                .pathMatchers(HttpMethod.POST, "/api/v1/dags/*/dagRuns").hasAnyRole("ADMIN", "OP", "USER")
                .pathMatchers(HttpMethod.GET, "/api/v1/dags/*/dagRuns/**").hasAnyRole("ADMIN", "OP", "USER")
                
                // DAG RUN control actions - USER and above
                // These endpoints allow controlling existing DAG runs
                .pathMatchers(HttpMethod.POST, "/api/v1/dags/*/dagRuns/*/clear").hasAnyRole("ADMIN", "OP", "USER")
                .pathMatchers(HttpMethod.PATCH, "/api/v1/dags/*/dagRuns/*/state").hasAnyRole("ADMIN", "OP", "USER")
                
                // DAG WRITE access - OP and ADMIN only
                // These endpoints allow modifying DAG configurations
                .pathMatchers(HttpMethod.PATCH, "/api/v1/dags/**").hasAnyRole("ADMIN", "OP")
                .pathMatchers(HttpMethod.DELETE, "/api/v1/dags/**").hasAnyRole("ADMIN", "OP")
                
                // Task instance operations - USER and above
                .pathMatchers(HttpMethod.GET, "/api/v1/dags/*/dagRuns/*/taskInstances").hasAnyRole("ADMIN", "OP", "USER")
                .pathMatchers(HttpMethod.PATCH, "/api/v1/dags/*/dagRuns/*/taskInstances/*/state").hasAnyRole("ADMIN", "OP", "USER")
                
                // Logs access - All authenticated users can access logs
                // Service layer will handle filtering based on user permissions
                .pathMatchers("/api/logs/**").authenticated()
                
                // Admin-only operations
                .pathMatchers("/api/admin/**").hasRole("ADMIN")
                
                // Default - Admin access for any unspecified endpoints
                .anyExchange().hasRole("ADMIN")
            )
            .addFilterAt(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
            .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
            .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
            .logout(ServerHttpSecurity.LogoutSpec::disable)
            .exceptionHandling(exceptionHandlingSpec -> 
                exceptionHandlingSpec
                    .authenticationEntryPoint((exchange, ex) -> {
                        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                        return Mono.empty();
                    })
                    .accessDeniedHandler((exchange, denied) -> {
                        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                        return Mono.empty();
                    })
            )
            .build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Collections.singletonList("http://localhost:5173"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(Collections.singletonList("*"));
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
} 