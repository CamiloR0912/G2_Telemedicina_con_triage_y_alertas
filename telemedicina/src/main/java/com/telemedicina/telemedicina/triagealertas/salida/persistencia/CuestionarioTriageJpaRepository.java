package com.telemedicina.telemedicina.triagealertas.infraestructura.salida.persistencia;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Detalle técnico: Spring Data JPA genera la implementación a partir de los nombres de los
 * métodos. Solo lo conoce {@link CuestionarioTriageRepositoryJpaAdapter}.
 */
interface CuestionarioTriageJpaRepository extends JpaRepository<CuestionarioTriageEntity, String> {

    List<CuestionarioTriageEntity> findByCitaId(String citaId);

    List<CuestionarioTriageEntity> findByAlertaIdIsNotNullAndAlertaAtendidaPorIsNull();
}
