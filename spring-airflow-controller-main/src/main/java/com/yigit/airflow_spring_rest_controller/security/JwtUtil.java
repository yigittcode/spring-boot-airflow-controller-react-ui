package com.yigit.airflow_spring_rest_controller.security;

import com.yigit.airflow_spring_rest_controller.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
@Slf4j
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;
    
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (SignatureException e) {
            log.error("JWT signature validation failed. Secret key may have been changed: {}", e.getMessage());
            throw e;
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
            throw e;
        } catch (MalformedJwtException e) {
            log.error("JWT token is malformed: {}", e.getMessage());
            throw e;
        } catch (UnsupportedJwtException e) {
            log.error("JWT token format is unsupported: {}", e.getMessage());
            throw e;
        } catch (JwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            throw e;
        }
    }

    private Boolean isTokenExpired(String token) {
        try {
            final Date expiration = extractExpiration(token);
            boolean expired = expiration.before(new Date());
            if (expired) {
                // Calculate how long ago the token expired
                long millisSinceExpired = System.currentTimeMillis() - expiration.getTime();
                String timeAgo = String.format("%.2f minutes ago", millisSinceExpired / (1000.0 * 60));
                log.info("Token expired {} for user {}", timeAgo, extractUsername(token));
            }
            return expired;
        } catch (ExpiredJwtException e) {
            log.error("Token is already expired: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error checking token expiration: {}", e.getMessage());
            return true; // Assume expired if we can't check
        }
    }

    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole().name());
        claims.put("airflowUsername", user.getAirflowUsername());
        claims.put("airflowPassword", user.getAirflowPassword());
        return createToken(claims, user.getUsername());
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            
            if (!username.equals(userDetails.getUsername())) {
                log.warn("Token validation failed: Username mismatch. Token: {}, UserDetails: {}", 
                        username, userDetails.getUsername());
                return false;
            }
            
            boolean expired = isTokenExpired(token);
            if (expired) {
                log.warn("Token validation failed: Token is expired for user {}", username);
                return false;
            }
            
            return true;
        } catch (ExpiredJwtException e) {
            log.error("Token expired for user: {}", e.getClaims().getSubject());
            throw e; // Rethrow to allow specific handling
        } catch (SignatureException e) {
            log.error("JWT signature validation failed. Secret key may have changed: {}", e.getMessage());
            throw e; // Rethrow to allow specific handling
        } catch (JwtException e) {
            log.error("JWT validation error: {}", e.getMessage());
            throw e; // Rethrow to allow specific handling
        } catch (Exception e) {
            log.error("Error validating token: {}", e.getMessage());
            return false;
        }
    }
    
    public String extractAirflowUsername(String token) {
        try {
            return (String) extractAllClaims(token).get("airflowUsername");
        } catch (JwtException e) {
            log.error("Could not extract airflow username from token: {}", e.getMessage());
            return null;
        }
    }
    
    public String extractAirflowPassword(String token) {
        try {
            return (String) extractAllClaims(token).get("airflowPassword");
        } catch (JwtException e) {
            log.error("Could not extract airflow password from token: {}", e.getMessage());
            return null;
        }
    }
} 