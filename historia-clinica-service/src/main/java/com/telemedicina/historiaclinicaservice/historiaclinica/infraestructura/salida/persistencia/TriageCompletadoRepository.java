package com.telemedicina.historiaclinicaservice.historiaclinica.infraestructura.salida.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;

/** Detalle técnico: solo lo conoce {@link TriagesCompletadosJpaAdapter}. */
interface TriageCompletadoRepository extends JpaRepository<TriageCompletadoJpaEntity, String> {
}
