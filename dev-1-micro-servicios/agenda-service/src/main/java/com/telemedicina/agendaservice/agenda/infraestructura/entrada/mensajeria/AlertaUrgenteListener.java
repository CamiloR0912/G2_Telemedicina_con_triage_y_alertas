package com.telemedicina.agendaservice.agenda.infraestructura.entrada.mensajeria;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.telemedicina.agendaservice.agenda.aplicacion.CitaUseCase;
import com.telemedicina.agendaservice.agenda.infraestructura.configuracion.RabbitConfig;

import tools.jackson.databind.ObjectMapper;

/**
 * Adaptador primario de mensajería: la alerta de urgencia alta que Triage publica en RabbitMQ
 * prioriza la cita (HU-03). Si Triage está caído simplemente no llegan mensajes; Agenda sigue
 * agendando igual (requisito no funcional).
 */
@Component
class AlertaUrgenteListener {

	private static final Logger log = LoggerFactory.getLogger(AlertaUrgenteListener.class);

	private final CitaUseCase citaUseCase;
	private final ObjectMapper objectMapper;

	AlertaUrgenteListener(CitaUseCase citaUseCase, ObjectMapper objectMapper) {
		this.citaUseCase = citaUseCase;
		this.objectMapper = objectMapper;
	}

	@RabbitListener(queues = RabbitConfig.COLA_AGENDA)
	void alRecibirAlerta(String mensaje) {
		String citaId = objectMapper.readTree(mensaje).get("citaId").asString();
		try {
			citaUseCase.priorizar(UUID.fromString(citaId));
			log.info("Alerta de urgencia alta: cita {} priorizada", citaId);
		} catch (RuntimeException e) {
			// Cita inexistente, cancelada o id inválido: reintentar no lo arregla, se descarta.
			log.warn("Alerta de urgencia alta para la cita {} descartada: {}", citaId, e.getMessage());
		}
	}
}
