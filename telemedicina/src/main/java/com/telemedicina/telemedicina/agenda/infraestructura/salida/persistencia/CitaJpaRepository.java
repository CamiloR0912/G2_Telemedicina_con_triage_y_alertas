package com.telemedicina.telemedicina.agenda.infraestructura.salida.persistencia;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Detalle técnico: Spring Data JPA genera la implementación a partir de los
 * nombres de los métodos. Solo lo conoce {@link CitaRepositoryJpaAdapter}.
 */
interface CitaJpaRepository extends JpaRepository<CitaEntity, UUID> {

	List<CitaEntity> findByMedicoIdOrPacienteId(String medicoId, String pacienteId);

	List<CitaEntity> findByMedicoId(String medicoId);
}
