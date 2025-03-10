package com.yigit.airflow_spring_rest_controller.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements WebFilter {

    private final JwtUtil jwtUtil;
    private final ReactiveUserDetailsService userDetailsService;
    
    @Value("${api.endpoint.prefix}")
    private String apiEndpointPrefix;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        // Skip authentication for the login endpoint
        if (request.getURI().getPath().equals(apiEndpointPrefix + "/auth/login")) {
            return chain.filter(exchange);
        }
        
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwt = authHeader.substring(7);
            
            try {
                String username = jwtUtil.extractUsername(jwt);

                if (username != null) {
                    return userDetailsService.findByUsername(username)
                            .filter(userDetails -> jwtUtil.validateToken(jwt, userDetails))
                            .map(userDetails -> new UsernamePasswordAuthenticationToken(
                                    userDetails, jwt, userDetails.getAuthorities()))
                            .flatMap(authentication -> chain.filter(exchange)
                                    .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication)));
                }
            } catch (Exception e) {
                // Log the error but continue the filter chain
                System.err.println("JWT validation error: " + e.getMessage());
            }
        }
        
        return chain.filter(exchange);
    }
} 