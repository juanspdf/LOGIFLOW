package com.logiflow.authservice_core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;


/**
 * Clase principal de Auth Service
 * Microservicio de Autenticación y Autorización para LogiFlow
 *
 * @author Guevara, Reyes, Pasquel
 * @version 1.0.0
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
public class AuthserviceCoreApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuthserviceCoreApplication.class, args);
	}

}
