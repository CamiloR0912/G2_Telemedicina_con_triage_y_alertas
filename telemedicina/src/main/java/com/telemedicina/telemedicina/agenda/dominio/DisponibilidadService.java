package com.telemedicina.telemedicina.agenda.dominio;

import java.util.Collection;
import java.util.Objects;

/**
 * Servicio de Dominio: decide si una franja horaria está libre.
 *
 * La regla involucra varias Citas a la vez (las del médico y las del paciente),
 * así que no pertenece naturalmente a ninguna Cita individual. Solo cuentan las
 * citas activas: una cita cancelada libera su cupo para otro paciente.
 */
public class DisponibilidadService {

	/** HU-02: lanza excepción si el médico o el paciente ya tienen una cita activa que se solape con la franja. */
	public void verificarDisponibilidad(String medicoId, String pacienteId, FranjaHoraria franja,
			Collection<Cita> citasExistentes) {
		Objects.requireNonNull(franja, "La franja horaria es obligatoria");
		for (Cita cita : citasExistentes) {
			if (!cita.ocupaCupoEn(franja)) {
				continue;
			}
			if (cita.getMedicoId().equals(medicoId)) {
				throw new FranjaNoDisponibleException(
						"El médico " + medicoId + " ya tiene una cita activa en esa franja horaria");
			}
			if (cita.getPacienteId().equals(pacienteId)) {
				throw new FranjaNoDisponibleException(
						"El paciente " + pacienteId + " ya tiene una cita activa en esa franja horaria");
			}
		}
	}

	public boolean estaDisponible(String medicoId, String pacienteId, FranjaHoraria franja,
			Collection<Cita> citasExistentes) {
		try {
			verificarDisponibilidad(medicoId, pacienteId, franja, citasExistentes);
			return true;
		} catch (FranjaNoDisponibleException e) {
			return false;
		}
	}

	/** HU-07: reprograma la cita solo si la nueva franja está libre, sin contar a la propia cita. */
	public void reprogramar(Cita cita, FranjaHoraria nuevaFranja, Collection<Cita> citasExistentes) {
		var otrasCitas = citasExistentes.stream()
				.filter(otra -> !otra.equals(cita))
				.toList();
		verificarDisponibilidad(cita.getMedicoId(), cita.getPacienteId(), nuevaFranja, otrasCitas);
		cita.reprogramar(nuevaFranja);
	}
}
