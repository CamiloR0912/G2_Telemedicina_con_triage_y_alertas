package com.telemedicina.agendaservice.agenda.infraestructura.entrada.web;

import com.telemedicina.agendaservice.agenda.dominio.Cita;

/** Traduce el modelo de dominio a lo que se expone por HTTP. */
final class CitaMapper {

	private CitaMapper() {
	}

	static CitaResponse aResponse(Cita cita) {
		return new CitaResponse(cita.getId(), cita.getPacienteId(), cita.getMedicoId(), cita.getEspecialidad(),
				cita.getFranja().inicio(), cita.getFranja().fin(), cita.getEstado().name(), cita.isPrioritaria());
	}
}
