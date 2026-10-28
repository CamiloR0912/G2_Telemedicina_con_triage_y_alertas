package com.telemedicina.historiaclinicaservice.historiaclinica.infraestructura.salida.persistencia;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/** Fila de la proyección local: esta cita ya tiene triage completado (dato que es de Triage, copiado aquí). */
@Entity
@Table(name = "triages_completados")
class TriageCompletadoJpaEntity {

    @Id
    private String citaId;

    private Instant recibidoEn;

    protected TriageCompletadoJpaEntity() {
    }

    TriageCompletadoJpaEntity(String citaId) {
        this.citaId = citaId;
        this.recibidoEn = Instant.now();
    }
}
