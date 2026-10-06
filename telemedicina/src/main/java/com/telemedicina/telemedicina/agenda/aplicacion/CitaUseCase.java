package com.telemedicina.telemedicina.agenda.aplicacion;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.telemedicina.telemedicina.agenda.dominio.Cita;

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
}
