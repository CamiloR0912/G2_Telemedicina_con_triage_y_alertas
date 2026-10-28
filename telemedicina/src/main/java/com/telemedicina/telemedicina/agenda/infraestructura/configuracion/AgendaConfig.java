package com.telemedicina.telemedicina.agenda.infraestructura.configuracion;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.telemedicina.telemedicina.agenda.dominio.CitaFactory;
import com.telemedicina.telemedicina.agenda.dominio.DisponibilidadService;

/**
 * Registra en Spring los objetos del dominio. Así el paquete dominio no importa
 * nada de Spring: se puede usar con {@code new} en un test sin contenedor.
 */
@Configuration
public class AgendaConfig {

	@Bean
	CitaFactory citaFactory() {
		return new CitaFactory();
	}

	@Bean
	DisponibilidadService disponibilidadService() {
		return new DisponibilidadService();
	}
}
