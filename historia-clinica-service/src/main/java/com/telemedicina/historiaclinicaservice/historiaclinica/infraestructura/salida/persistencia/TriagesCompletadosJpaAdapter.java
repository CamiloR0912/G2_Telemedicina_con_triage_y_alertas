package com.telemedicina.historiaclinicaservice.historiaclinica.infraestructura.salida.persistencia;

import com.telemedicina.historiaclinicaservice.historiaclinica.aplicacion.TriagesCompletados;

import org.springframework.stereotype.Component;

/** Adaptador secundario del puerto {@link TriagesCompletados} sobre Spring Data JPA. */
@Component
class TriagesCompletadosJpaAdapter implements TriagesCompletados {

    private final TriageCompletadoRepository repositorio;

    TriagesCompletadosJpaAdapter(TriageCompletadoRepository repositorio) {
        this.repositorio = repositorio;
    }

    /** Idempotente: si Kafka entrega el mismo evento dos veces, la cita sigue apareciendo una sola vez. */
    @Override
    public void registrar(String citaId) {
        if (!repositorio.existsById(citaId)) {
            repositorio.save(new TriageCompletadoJpaEntity(citaId));
        }
    }

    @Override
    public boolean estaCompletado(String citaId) {
        return repositorio.existsById(citaId);
    }
}
