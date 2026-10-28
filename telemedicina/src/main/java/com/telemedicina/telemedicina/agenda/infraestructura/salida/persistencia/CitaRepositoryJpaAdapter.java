package com.telemedicina.telemedicina.agenda.infraestructura.salida.persistencia;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.telemedicina.telemedicina.agenda.aplicacion.RepositorioCitas;
import com.telemedicina.telemedicina.agenda.dominio.Cita;

/**
 * Adaptador secundario: traduce el puerto {@link RepositorioCitas} (que diseñó el
 * núcleo) a llamadas de Spring Data JPA, y {@link CitaEntity} de ida y vuelta a {@link Cita}.
 */
@Component
public class CitaRepositoryJpaAdapter implements RepositorioCitas {

	private final CitaJpaRepository citaJpaRepository;

	CitaRepositoryJpaAdapter(CitaJpaRepository citaJpaRepository) {
		this.citaJpaRepository = citaJpaRepository;
	}

	@Override
	public Cita guardar(Cita cita) {
		return citaJpaRepository.save(CitaEntity.desde(cita)).aDominio();
	}

	@Override
	public Optional<Cita> buscarPorId(UUID id) {
		return citaJpaRepository.findById(id).map(CitaEntity::aDominio);
	}

	@Override
	public List<Cita> buscarPorMedicoOPaciente(String medicoId, String pacienteId) {
		return citaJpaRepository.findByMedicoIdOrPacienteId(medicoId, pacienteId).stream()
				.map(CitaEntity::aDominio)
				.toList();
	}

	@Override
	public List<Cita> buscarPorMedico(String medicoId) {
		return citaJpaRepository.findByMedicoId(medicoId).stream()
				.map(CitaEntity::aDominio)
				.toList();
	}
}
