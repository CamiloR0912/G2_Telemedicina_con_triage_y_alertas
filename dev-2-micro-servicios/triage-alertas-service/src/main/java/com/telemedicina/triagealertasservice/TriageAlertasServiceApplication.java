package com.telemedicina.triagealertasservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/** {@code @EnableScheduling} activa el {@code OutboxPoller} que publica los eventos pendientes en Kafka. */
@SpringBootApplication
@EnableScheduling
public class TriageAlertasServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(TriageAlertasServiceApplication.class, args);
	}

}
