package com.telemedicina.telemedicina.agenda;

/**
 * En el contexto de Agenda, "estado" siempre significa el estado de la Cita.
 */
public enum EstadoCita {
	AGENDADA,
	REPROGRAMADA,
	CANCELADA;

	/** Una cita activa ocupa su cupo; una cancelada lo libera para otro paciente. */
	public boolean esActiva() {
		return this != CANCELADA;
	}
}
