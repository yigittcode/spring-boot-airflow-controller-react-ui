package com.yigit.airflow_spring_rest_controller.security;

import com.yigit.airflow_spring_rest_controller.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Collections;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserDetailsServiceImpl implements ReactiveUserDetailsService {

    private final UserRepository userRepository;

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        log.debug("Attempting to find user by username: {}", username);
        return userRepository.findByUsername(username)
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("User not found: {}", username);
                    return Mono.error(new UsernameNotFoundException("User not found: " + username));
                }))
                .map(user -> {
                    log.debug("User found: {} with role {}", username, user.getRole().name());
                    return User.builder()
                            .username(user.getUsername())
                            .password(user.getPassword())
                            .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())))
                            .disabled(!user.getIsActive())
                            .accountExpired(false)
                            .credentialsExpired(false)
                            .accountLocked(false)
                            .build();
                });
    }
} 