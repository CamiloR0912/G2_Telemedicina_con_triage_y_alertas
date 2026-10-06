package com.telemedicina.telemedicina.agenda.aplicacion;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.telemedicina.telemedicina.agenda.dominio.Cita;

/** Test double hecho a mano: cumple el puerto guardando las citas en un Map. */
class RepositorioCitasFalso implements RepositorioCitas {

	private final Map<UUID, Cita> almacen = new LinkedHashMap<>();

	@Override
	public Cita guardar(Cita cita) {
		almacen.put(cita.getId(), cita);
		return cita;
	}

	@Override
	public Optional<Cita> buscarPorId(UUID id) {
		return Optional.ofNullable(almacen.get(id));
	}

	@Override
	public List<Cita> buscarPorMedicoOPaciente(String medicoId, String pacienteId) {
		return almacen.values().stream()
				.filter(cita -> cita.getMedicoId().equals(medicoId) || cita.getPacienteId().equals(pacienteId))
				.toList();
	}

	@Override
	public List<Cita> buscarPorMedico(String medicoId) {
		return almacen.values().stream()
				.filter(cita -> cita.getMedicoId().equals(medicoId))
				.toList();
	}
}
