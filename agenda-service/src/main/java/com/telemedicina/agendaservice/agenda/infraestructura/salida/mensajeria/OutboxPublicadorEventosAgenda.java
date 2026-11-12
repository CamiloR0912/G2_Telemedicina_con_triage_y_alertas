package com.telemedicina.agendaservice.agenda.infraestructura.salida.mensajeria;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.telemedicina.agendaservice.agenda.aplicacion.PublicadorEventosAgenda;
import com.telemedicina.agendaservice.agenda.dominio.Cita;

import tools.jackson.databind.ObjectMapper;

/**
 * Adaptador secundario del puerto {@link PublicadorEventosAgenda}: no habla con Kafka, escribe en la
 * tabla outbox dentro de la transacción del caso de uso. {@link OutboxPoller} lo envía después.
 */
@Component
class OutboxPublicadorEventosAgenda implements PublicadorEventosAgenda {

	static final String TOPICO_CITA_EN_CANCELACION = "citas.en-cancelacion";

	private final OutboxEventRepository repositorio;
	private final ObjectMapper objectMapper;

	OutboxPublicadorEventosAgenda(OutboxEventRepository repositorio, ObjectMapper objectMapper) {
		this.repositorio = repositorio;
		this.objectMapper = objectMapper;
	}

	@Override
	public void citaEnCancelacion(Cita cita) {
		String citaId = cita.getId().toString();
		String payload = objectMapper.writeValueAsString(Map.of(
				"citaId", citaId,
				"pacienteId", cita.getPacienteId(),
				"medicoId", cita.getMedicoId()));
		// La clave es la cita: todos los eventos de una misma cita caen en la misma partición, en orden.
		repositorio.save(new OutboxEvent(TOPICO_CITA_EN_CANCELACION, citaId, payload));
	}
}
