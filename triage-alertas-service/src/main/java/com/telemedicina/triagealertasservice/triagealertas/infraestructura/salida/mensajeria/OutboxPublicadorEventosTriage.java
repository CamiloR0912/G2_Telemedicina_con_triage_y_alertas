package com.telemedicina.triagealertasservice.triagealertas.infraestructura.salida.mensajeria;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.telemedicina.triagealertasservice.triagealertas.aplicacion.PublicadorEventosTriage;
import com.telemedicina.triagealertasservice.triagealertas.dominio.CuestionarioTriage;
import com.telemedicina.triagealertasservice.triagealertas.dominio.NivelUrgencia;

import tools.jackson.databind.ObjectMapper;

/**
 * Adaptador secundario del puerto {@link PublicadorEventosTriage}: no habla con Kafka, escribe en la
 * tabla outbox dentro de la transacción del caso de uso. {@link OutboxPoller} lo envía después.
 */
@Component
class OutboxPublicadorEventosTriage implements PublicadorEventosTriage {

    static final String TOPICO_TRIAGE_COMPLETADO = "triage.completados";

    private final OutboxEventRepository repositorio;
    private final ObjectMapper objectMapper;

    OutboxPublicadorEventosTriage(OutboxEventRepository repositorio, ObjectMapper objectMapper) {
        this.repositorio = repositorio;
        this.objectMapper = objectMapper;
    }

    @Override
    public void triageCompletado(CuestionarioTriage cuestionario) {
        String payload = objectMapper.writeValueAsString(Map.of(
                "cuestionarioId", cuestionario.getId(),
                "citaId", cuestionario.getCitaId(),
                "pacienteId", cuestionario.getPacienteId(),
                "nivelUrgencia", cuestionario.getNivelUrgencia().map(NivelUrgencia::valor).orElseThrow()));
        // La clave es la cita: todos los eventos de una misma cita caen en la misma partición, en orden.
        repositorio.save(new OutboxEvent(TOPICO_TRIAGE_COMPLETADO, cuestionario.getCitaId(), payload));
    }
}
