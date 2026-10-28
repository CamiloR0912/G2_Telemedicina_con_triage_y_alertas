package com.telemedicina.agendaservice.agenda.aplicacion;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.telemedicina.agendaservice.agenda.dominio.Cita;

/**
 * Puerto primario de Agenda: lo que el subdominio promete a quien lo llame
 * (hoy el controlador REST, mañana cualquier otro adaptador de entrada).
 */
public interface CitaUseCase {

	/** HU-02: agenda una cita por especialidad en una franja libre. */
	Cita agendar(String pacienteId, String medicoId, String especialidad, LocalDateTime inicio, LocalDateTime fin);

	Cita buscarPorId(UUID id);

	/** HU-07: cancela la cita y libera su cupo. */
	Cita cancelar(UUID id);

	/** HU-07: mueve la cita a otra franja libre. */
	Cita reprogramar(UUID id, LocalDateTime inicio, LocalDateTime fin);

	/** HU-08: citas activas del médico en un día, ordenadas por hora de inicio. */
	List<Cita> agendaDelDia(String medicoId, LocalDate dia);

	/** HU-03: la alerta de urgencia alta que llega de Triage prioriza la cita del paciente. */
	Cita priorizar(UUID id);

	/**
	 * HU-06 (consumido por Historia clínica vía gRPC): ¿hay una cita no cancelada, futura o pasada,
	 * entre el médico y el paciente? Una cancelada nunca llegó a ser consulta, así que no cuenta.
	 */
	boolean existeCitaEntre(String medicoId, String pacienteId);
}
