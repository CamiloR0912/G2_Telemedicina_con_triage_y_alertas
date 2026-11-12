package com.telemedicina.triagealertasservice.triagealertas.infraestructura.salida.mensajeria;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.telemedicina.triagealertasservice.triagealertas.aplicacion.PublicadorRespuestaCancelacion;

import tools.jackson.databind.ObjectMapper;

/**
 * Adaptador secundario del puerto {@link PublicadorRespuestaCancelacion}: escribe la respuesta de la
 * Saga en la misma tabla outbox que ya usa Triage; {@link OutboxPoller} la envía a Kafka.
 */
@Component
class OutboxPublicadorRespuestaCancelacion implements PublicadorRespuestaCancelacion {

    static final String TOPICO_CONFIRMADA = "citas.cancelacion-confirmada";
    static final String TOPICO_RECHAZADA = "citas.cancelacion-rechazada";

    private final OutboxEventRepository repositorio;
    private final ObjectMapper objectMapper;

    OutboxPublicadorRespuestaCancelacion(OutboxEventRepository repositorio, ObjectMapper objectMapper) {
        this.repositorio = repositorio;
        this.objectMapper = objectMapper;
    }

    @Override
    public void cancelacionConfirmada(String citaId) {
        guardar(TOPICO_CONFIRMADA, citaId, Map.of("citaId", citaId));
    }

    @Override
    public void cancelacionRechazada(String citaId, String motivo) {
        guardar(TOPICO_RECHAZADA, citaId, Map.of("citaId", citaId, "motivo", motivo));
    }

    private void guardar(String topico, String citaId, Map<String, String> datos) {
        repositorio.save(new OutboxEvent(topico, citaId, objectMapper.writeValueAsString(datos)));
    }
}
