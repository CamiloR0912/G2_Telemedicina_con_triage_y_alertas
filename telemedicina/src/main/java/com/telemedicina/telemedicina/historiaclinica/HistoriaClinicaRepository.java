package com.telemedicina.telemedicina.historiaclinica;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** Detalle técnico: Spring Data JPA genera la implementación en tiempo de ejecución. */
public interface HistoriaClinicaRepository extends JpaRepository<HistoriaClinicaJpaEntity, String> {

    Optional<HistoriaClinicaJpaEntity> findByPacienteId(String pacienteId);

    boolean existsByPacienteId(String pacienteId);
}
