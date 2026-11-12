package com.telemedicina.agendaservice.agenda.infraestructura.entrada.mensajeria;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.telemedicina.agendaservice.agenda.aplicacion.CitaUseCase;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * Adaptador primario de mensajería: último eslabón de la Saga de cancelación. Triage responde por
 * Kafka si la cita puede cancelarse; aquí se confirma la cancelación o se compensa.
 */
@Component
class RespuestaCancelacionListener {

	private static final Logger log = LoggerFactory.getLogger(RespuestaCancelacionListener.class);

	private final CitaUseCase citaUseCase;
	private final ObjectMapper objectMapper;

	RespuestaCancelacionListener(CitaUseCase citaUseCase, ObjectMapper objectMapper) {
		this.citaUseCase = citaUseCase;
		this.objectMapper = objectMapper;
	}

	@KafkaListener(topics = "citas.cancelacion-confirmada")
	void alConfirmarse(String mensaje) {
		String citaId = objectMapper.readTree(mensaje).get("citaId").asString();
		try {
			citaUseCase.confirmarCancelacion(UUID.fromString(citaId));
			log.info("Saga: Triage confirmó, la cita {} queda CANCELADA y libera su cupo", citaId);
		} catch (RuntimeException e) {
			// Evento repetido o cita inexistente: reintentar no lo arregla, se descarta.
			log.warn("Saga: confirmación para la cita {} descartada: {}", citaId, e.getMessage());
		}
	}

	@KafkaListener(topics = "citas.cancelacion-rechazada")
	void alRechazarse(String mensaje) {
		JsonNode evento = objectMapper.readTree(mensaje);
		String citaId = evento.get("citaId").asString();
		try {
			var cita = citaUseCase.compensarCancelacion(UUID.fromString(citaId));
			log.info("Saga: Triage rechazó ({}). Compensación: la cita {} vuelve a {}",
					evento.get("motivo").asString(), citaId, cita.getEstado());
		} catch (RuntimeException e) {
			log.warn("Saga: rechazo para la cita {} descartado: {}", citaId, e.getMessage());
		}
	}
}
