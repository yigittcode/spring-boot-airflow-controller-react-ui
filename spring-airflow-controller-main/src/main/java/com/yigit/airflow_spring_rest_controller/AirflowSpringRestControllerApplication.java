package com.yigit.airflow_spring_rest_controller;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

@SpringBootApplication
@EnableR2dbcRepositories
public class AirflowSpringRestControllerApplication {

	public static void main(String[] args) {
		SpringApplication.run(AirflowSpringRestControllerApplication.class, args);
	}

}
