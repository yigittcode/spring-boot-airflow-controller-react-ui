package com.yigit.airflow_spring_rest_controller.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import io.swagger.v3.oas.models.ExternalDocumentation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springdoc.core.models.GroupedOpenApi;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("airflow-public")
                .pathsToMatch("/api/v1/**")
                .build();
    }

    @Bean
    public OpenAPI airflowApiDoc() {
        Server devServer = new Server()
            .url("http://localhost:8008")
            .description("Development server");

        Contact contact = new Contact()
            .name("Yigit")
            .email("your.email@example.com")
            .url("https://github.com/yourusername");

        License license = new License()
            .name("Apache 2.0")
            .url("http://www.apache.org/licenses/LICENSE-2.0.html");

        Info info = new Info()
            .title("Airflow REST API")
            .version("1.0.0")
            .description("REST API for Apache Airflow operations. This API provides endpoints to manage DAGs, DAG Runs, and Task Instances.")
            .contact(contact)
            .license(license);

        return new OpenAPI()
            .info(info)
            .servers(List.of(devServer))
            .tags(List.of(
                new Tag().name("DAGs").description("Operations about DAGs"),
                new Tag().name("DAG Runs").description("Operations about DAG Runs"),
                new Tag().name("Task Instances").description("Operations about Task Instances")
            ))
            .externalDocs(new ExternalDocumentation()
                .description("Apache Airflow Documentation")
                .url("https://airflow.apache.org/docs/"));
    }
} 