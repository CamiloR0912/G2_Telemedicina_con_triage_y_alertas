package com.telemedicina.agendaservice.agenda.dominio;

/**
 * En el contexto de Agenda, "estado" siempre significa el estado de la Cita.
 */
public enum EstadoCita {
	AGENDADA,
	REPROGRAMADA,
	/** Saga de cancelación en curso: espera la respuesta de Triage y sigue ocupando su cupo. */
	EN_CANCELACION,
	CANCELADA;

	/** Una cita activa ocupa su cupo; una cancelada lo libera para otro paciente. */
	public boolean esActiva() {
		return this != CANCELADA;
	}
}
