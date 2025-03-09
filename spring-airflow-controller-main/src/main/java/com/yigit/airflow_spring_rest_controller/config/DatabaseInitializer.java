package com.yigit.airflow_spring_rest_controller.config;

import com.yigit.airflow_spring_rest_controller.entity.Role;
import com.yigit.airflow_spring_rest_controller.entity.User;
import com.yigit.airflow_spring_rest_controller.repository.UserRepository;
import io.r2dbc.spi.ConnectionFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Flux;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DatabaseInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public ConnectionFactoryInitializer initializer(ConnectionFactory connectionFactory) {
        ConnectionFactoryInitializer initializer = new ConnectionFactoryInitializer();
        initializer.setConnectionFactory(connectionFactory);
        initializer.setDatabasePopulator(new ResourceDatabasePopulator(new ClassPathResource("db/migration/schema.sql")));
        return initializer;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initializeUsersAfterStartup() {
        userRepository.count()
                .flatMapMany(count -> {
                    if (count == 0) {
                        log.info("Initializing database with default users");
                        return createDefaultUsers();
                    } else {
                        log.info("Database already contains {} users, skipping initialization", count);
                        return Flux.empty();
                    }
                })
                .subscribe();
    }

    private Flux<User> createDefaultUsers() {
        String encodedPassword = passwordEncoder.encode("admin123");
        
        // Create the admin user
        User admin = User.builder()
                .firstName("Sistem")
                .lastName("Yönetici")
                .username("admin")
                .email("admin@example.com")
                .password(encodedPassword)
                .isActive(true)
                .role(Role.ADMIN)
                .airflowUsername("admin")
                .airflowPassword("admin123")
                .build();
        
        // Create the admin_user
        User adminUser = User.builder()
                .firstName("Admin")
                .lastName("User")
                .username("admin_user")
                .email("admin_user@example.com")
                .password(encodedPassword)
                .isActive(true)
                .role(Role.ADMIN)
                .airflowUsername("admin")
                .airflowPassword("admin123")
                .build();
        
        // Create the regular user
        User regularUser = User.builder()
                .firstName("User")
                .lastName("User")
                .username("user_user")
                .email("user_user@example.com")
                .password(encodedPassword)
                .isActive(true)
                .role(Role.USER)
                .airflowUsername("admin")
                .airflowPassword("admin123")
                .build();
        
        // Create the viewer user
        User viewerUser = User.builder()
                .firstName("Viewer")
                .lastName("User")
                .username("viewer_user")
                .email("viewer_user@example.com")
                .password(encodedPassword)
                .isActive(true)
                .role(Role.VIEWER)
                .airflowUsername("admin")
                .airflowPassword("admin123")
                .build();
        
        // Create the op user
        User opUser = User.builder()
                .firstName("Op")
                .lastName("User")
                .username("op_user")
                .email("op_user@example.com")
                .password(encodedPassword)
                .isActive(true)
                .role(Role.OP)
                .airflowUsername("admin")
                .airflowPassword("admin123")
                .build();
        
        // Create the public user
        User publicUser = User.builder()
                .firstName("Public")
                .lastName("User")
                .username("public_user")
                .email("public_user@example.com")
                .password(encodedPassword)
                .isActive(true)
                .role(Role.PUBLIC)
                .airflowUsername("admin")
                .airflowPassword("admin123")
                .build();
        
        return userRepository.saveAll(Flux.just(admin, adminUser, regularUser, viewerUser, opUser, publicUser));
    }
} 