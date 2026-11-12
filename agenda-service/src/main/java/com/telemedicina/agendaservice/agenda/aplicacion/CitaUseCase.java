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

	/**
	 * HU-07 · inicia la Saga de cancelación: la cita queda EN_CANCELACION y se avisa a Triage.
	 * El cupo se libera recién cuando Triage confirma ({@link #confirmarCancelacion}).
	 */
	Cita cancelar(UUID id);

	/** Saga · Triage no encontró alertas urgentes pendientes: la cita se cancela y libera su cupo. */
	Cita confirmarCancelacion(UUID id);

	/** Saga · compensación: Triage rechazó la cancelación y la cita vuelve a su estado anterior. */
	Cita compensarCancelacion(UUID id);

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
