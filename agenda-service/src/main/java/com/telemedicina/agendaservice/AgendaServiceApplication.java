package com.telemedicina.agendaservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/** {@code @EnableScheduling} activa el {@code OutboxPoller} que publica los eventos de la Saga en Kafka. */
@SpringBootApplication
@EnableScheduling
public class AgendaServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AgendaServiceApplication.class, args);
	}

}
