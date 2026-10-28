package com.telemedicina.agendaservice.agenda.aplicacion;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.telemedicina.agendaservice.agenda.dominio.Cita;

/**
 * Puerto secundario mínimo: solo los métodos que {@link CitaService} usa de verdad,
 * no los que JpaRepository regala.
 */
public interface RepositorioCitas {

	Cita guardar(Cita cita);

	Optional<Cita> buscarPorId(UUID id);

	/** Citas donde participa el médico o el paciente: las que pueden chocar en disponibilidad. */
	List<Cita> buscarPorMedicoOPaciente(String medicoId, String pacienteId);

	List<Cita> buscarPorMedico(String medicoId);
}
