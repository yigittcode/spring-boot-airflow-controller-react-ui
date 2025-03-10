package com.yigit.airflow_spring_rest_controller.security;

import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class JwtAuthenticationFilter implements WebFilter {

    private final JwtUtil jwtUtil;
    private final ReactiveUserDetailsService userDetailsService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        
        // Skip authentication for login endpoint
        if (path.contains("/login")) {
            log.debug("Skipping authentication for login path: {}", path);
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwt = authHeader.substring(7);
            try {
                String username = jwtUtil.extractUsername(jwt);
                
                if (username != null) {
                    log.debug("Processing JWT token for user: {}", username);
                    
                    return userDetailsService.findByUsername(username)
                            .flatMap(userDetails -> {
                                if (jwtUtil.validateToken(jwt, userDetails)) {
                                    log.debug("Valid JWT token for user: {}", username);
                                    
                                    UsernamePasswordAuthenticationToken authentication = 
                                            new UsernamePasswordAuthenticationToken(
                                                    userDetails, jwt, userDetails.getAuthorities());
                                    
                                    return chain.filter(exchange)
                                            .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
                                }
                                log.warn("Invalid JWT token for user: {}", username);
                                return chain.filter(exchange);
                            });
                }
            } catch (JwtException e) {
                log.error("JWT validation error: {}", e.getMessage());
            } catch (Exception e) {
                log.error("Error during authentication: {}", e.getMessage());
            }
        } else {
            log.debug("No JWT token found in request headers");
        }
        
        return chain.filter(exchange);
    }
} 